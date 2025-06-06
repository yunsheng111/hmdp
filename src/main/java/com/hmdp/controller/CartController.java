package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.AddToCartDTO;
import com.hmdp.dto.CreateOrderDTO;
import com.hmdp.service.ICartService;
import com.hmdp.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * <p>
 * 购物车管理控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@RestController
@RequestMapping("/cart")
@Api(tags = "购物车管理")
public class CartController {

    @Resource
    private ICartService cartService;

    @Resource
    private IOrderService orderService;

    /**
     * 获取当前用户购物车
     * @return 购物车信息
     */
    @GetMapping
    @ApiOperation("获取当前用户购物车")
    public Result getCurrentUserCart() {
        return cartService.getCurrentUserCart();
    }

    /**
     * 添加商品到购物车
     * @param dto 添加到购物车的商品信息
     * @return 操作结果
     */
    @PostMapping("/item")
    @ApiOperation("添加商品到购物车")
    public Result addToCart(@Valid @RequestBody AddToCartDTO dto) {
        log.info("添加商品到购物车: {}", dto);
        return cartService.addToCart(dto);
    }

    /**
     * 更新购物车项数量
     * @param id 购物车项ID
     * @param requestBody 包含数量的请求体
     * @return 操作结果
     */
    @PutMapping("/item/{id}")
    @ApiOperation("更新购物车项数量")
    public Result updateCartItemQuantity(@PathVariable("id") Long id, 
                                       @RequestBody Map<String, Integer> requestBody) {
        Integer quantity = requestBody.get("quantity");
        if (quantity == null || quantity <= 0) {
            return Result.fail("数量必须大于0");
        }
        return cartService.updateCartItemQuantity(id, quantity);
    }

    /**
     * 删除购物车项
     * @param id 购物车项ID
     * @return 操作结果
     */
    @DeleteMapping("/item/{id}")
    @ApiOperation("删除购物车项")
    public Result removeCartItem(@PathVariable("id") Long id) {
        return cartService.removeCartItem(id);
    }

    /**
     * 清空购物车
     * @return 操作结果
     */
    @DeleteMapping
    @ApiOperation("清空购物车")
    public Result clearCart() {
        return cartService.clearCurrentUserCart();
    }

    /**
     * 购物车结算
     * @param createOrderDTO 创建订单请求参数
     * @return 订单ID列表
     */
    @PostMapping("/checkout")
    @ApiOperation("购物车结算")
    public Result checkoutCart(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        log.info("购物车结算: {}", createOrderDTO);
        return orderService.createOrderFromCart(createOrderDTO);
    }
}
