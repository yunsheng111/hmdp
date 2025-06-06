package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.AddToCartDTO;
import com.hmdp.dto.CartDTO;
import com.hmdp.dto.CartItemDTO;

import com.hmdp.entity.Cart;
import com.hmdp.entity.CartItem;
import com.hmdp.entity.Product;
import com.hmdp.mapper.CartMapper;
import com.hmdp.mapper.CartItemMapper;
import com.hmdp.mapper.ProductMapper;
import com.hmdp.service.ICartService;

import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 购物车主表 服务实现类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @Resource
    private CartItemMapper cartItemMapper;

    @Resource
    private ProductMapper productMapper;



    @Override
    @Transactional
    public Result addToCart(AddToCartDTO dto) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 验证商品是否存在且上架
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            return Result.fail("商品不存在");
        }
        if (product.getStatus() != 1) {
            return Result.fail("商品已下架");
        }
        if (product.getStock() < dto.getQuantity()) {
            return Result.fail("商品库存不足");
        }

        // 3. 获取或创建用户购物车
        Cart cart = getOrCreateCart(userId);

        // 4. 检查购物车中是否已有该商品
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getId())
                   .eq("product_id", dto.getProductId());
        CartItem existingItem = cartItemMapper.selectOne(queryWrapper);

        if (existingItem != null) {
            // 更新数量
            int newQuantity = existingItem.getQuantity() + dto.getQuantity();
            if (newQuantity > product.getStock()) {
                return Result.fail("商品库存不足");
            }
            existingItem.setQuantity(newQuantity);
            cartItemMapper.updateById(existingItem);
        } else {
            // 新增购物车项
            CartItem cartItem = new CartItem();
            cartItem.setCartId(cart.getId());
            cartItem.setProductId(dto.getProductId());
            cartItem.setQuantity(dto.getQuantity());
            cartItem.setSpecifications(dto.getSpecifications());
            cartItem.setCreateTime(LocalDateTime.now());
            cartItemMapper.insert(cartItem);
        }

        // 5. 更新购物车时间
        cart.setUpdateTime(LocalDateTime.now());
        updateById(cart);

        return Result.success("添加成功");
    }

    @Override
    public Result updateCartItemQuantity(Long itemId, Integer quantity) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 验证购物车项是否属于当前用户
        CartItem cartItem = cartItemMapper.selectById(itemId);
        if (cartItem == null) {
            return Result.fail("购物车项不存在");
        }

        Cart cart = getById(cartItem.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.fail("无权限操作");
        }

        // 3. 验证商品库存
        Product product = productMapper.selectById(cartItem.getProductId());
        if (product == null || product.getStatus() != 1) {
            return Result.fail("商品已下架");
        }
        if (product.getStock() < quantity) {
            return Result.fail("商品库存不足");
        }

        // 4. 更新数量
        cartItem.setQuantity(quantity);
        cartItemMapper.updateById(cartItem);

        // 5. 更新购物车时间
        cart.setUpdateTime(LocalDateTime.now());
        updateById(cart);

        return Result.success("更新成功");
    }

    @Override
    public Result removeCartItem(Long itemId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 2. 验证购物车项是否属于当前用户
        CartItem cartItem = cartItemMapper.selectById(itemId);
        if (cartItem == null) {
            return Result.fail("购物车项不存在");
        }

        Cart cart = getById(cartItem.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.fail("无权限操作");
        }

        // 3. 删除购物车项
        cartItemMapper.deleteById(itemId);

        // 4. 更新购物车时间
        cart.setUpdateTime(LocalDateTime.now());
        updateById(cart);

        return Result.success("删除成功");
    }

    @Override
    public Result getCartByUserId(Long userId) {
        // 1. 获取用户购物车
        QueryWrapper<Cart> cartQuery = new QueryWrapper<>();
        cartQuery.eq("user_id", userId);
        Cart cart = getOne(cartQuery);

        if (cart == null) {
            // 返回空购物车
            CartDTO cartDTO = new CartDTO();
            cartDTO.setUserId(userId);
            cartDTO.setItems(new ArrayList<>());
            cartDTO.setTotalPrice(0L);
            cartDTO.setTotalQuantity(0);
            return Result.success(cartDTO);
        }

        return buildCartDTO(cart);
    }

    @Override
    public Result getCurrentUserCart() {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public Result clearCart(Long userId) {
        // 1. 获取用户购物车
        QueryWrapper<Cart> cartQuery = new QueryWrapper<>();
        cartQuery.eq("user_id", userId);
        Cart cart = getOne(cartQuery);

        if (cart != null) {
            // 2. 删除所有购物车项
            QueryWrapper<CartItem> itemQuery = new QueryWrapper<>();
            itemQuery.eq("cart_id", cart.getId());
            cartItemMapper.delete(itemQuery);

            // 3. 更新购物车时间
            cart.setUpdateTime(LocalDateTime.now());
            updateById(cart);
        }

        return Result.success("清空成功");
    }

    @Override
    public Result clearCurrentUserCart() {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        return clearCart(userId);
    }

    /**
     * 获取或创建用户购物车
     */
    private Cart getOrCreateCart(Long userId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Cart cart = getOne(queryWrapper);

        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            save(cart);
        }

        return cart;
    }

    /**
     * 构建购物车DTO
     */
    private Result buildCartDTO(Cart cart) {
        // 1. 获取购物车项详情
        List<CartItemDTO> items = cartItemMapper.selectCartItemsWithProduct(cart.getId());

        // 2. 计算总价和总数量
        long totalPrice = 0;
        int totalQuantity = 0;
        for (CartItemDTO item : items) {
            totalPrice += item.getSubtotalPrice();
            totalQuantity += item.getQuantity();
        }

        // 3. 构建DTO
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUserId());
        cartDTO.setItems(items);
        cartDTO.setTotalPrice(totalPrice);
        cartDTO.setTotalQuantity(totalQuantity);
        cartDTO.setCreateTime(cart.getCreateTime());
        cartDTO.setUpdateTime(cart.getUpdateTime());

        return Result.success(cartDTO);
    }


}
