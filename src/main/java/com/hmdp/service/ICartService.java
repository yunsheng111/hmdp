package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.dto.AddToCartDTO;
import com.hmdp.entity.Cart;

/**
 * <p>
 * 购物车主表 服务类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface ICartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     * @param dto 添加到购物车的商品信息
     * @return 操作结果
     */
    Result addToCart(AddToCartDTO dto);

    /**
     * 更新购物车项数量
     * @param itemId 购物车项ID
     * @param quantity 新数量
     * @return 操作结果
     */
    Result updateCartItemQuantity(Long itemId, Integer quantity);

    /**
     * 删除购物车项
     * @param itemId 购物车项ID
     * @return 操作结果
     */
    Result removeCartItem(Long itemId);

    /**
     * 获取用户购物车
     * @param userId 用户ID
     * @return 购物车信息
     */
    Result getCartByUserId(Long userId);

    /**
     * 获取当前用户购物车
     * @return 购物车信息
     */
    Result getCurrentUserCart();

    /**
     * 清空购物车
     * @param userId 用户ID
     * @return 操作结果
     */
    Result clearCart(Long userId);

    /**
     * 清空当前用户购物车
     * @return 操作结果
     */
    Result clearCurrentUserCart();


}
