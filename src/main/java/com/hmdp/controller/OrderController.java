package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.CreateOrderDTO;
import com.hmdp.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 订单信息表 前端控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@RestController
@RequestMapping("/order")
@Api(tags = "订单管理")
public class OrderController {

    @Resource
    private IOrderService orderService;

    /**
     * 从购物车结算创建订单
     * @param createOrderDTO 创建订单请求参数
     * @return 订单ID列表
     */
    @PostMapping("/checkout")
    @ApiOperation("从购物车结算创建订单")
    public Result checkout(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        return orderService.createOrderFromCart(createOrderDTO);
    }

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取订单详情")
    public Result getOrderById(@PathVariable("id") Long id) {
        return orderService.getOrderById(id);
    }

    /**
     * 获取当前用户订单列表
     * @param status 订单状态（可选）
     * @param current 当前页
     * @param size 页大小
     * @return 订单列表
     */
    @GetMapping("/list")
    @ApiOperation("获取当前用户订单列表")
    public Result getCurrentUserOrders(
            @ApiParam("订单状态：1-待支付，2-已支付，3-已完成，4-已取消，5-已退款") @RequestParam(required = false) Integer status,
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("页大小") @RequestParam(defaultValue = "10") Integer size) {
        return orderService.getCurrentUserOrders(status, current, size);
    }

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 新状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新订单状态")
    public Result updateOrderStatus(
            @PathVariable("id") Long id,
            @ApiParam("订单状态：1-待支付，2-已支付，3-已完成，4-已取消，5-已退款") @RequestParam Integer status) {
        return orderService.updateOrderStatus(id, status);
    }

    /**
     * 取消订单
     * @param id 订单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("取消订单")
    public Result cancelOrder(@PathVariable("id") Long id) {
        return orderService.cancelOrder(id);
    }

    /**
     * 支付订单
     * @param id 订单ID
     * @return 操作结果
     */
    @PostMapping("/{id}/pay")
    @ApiOperation("支付订单")
    public Result payOrder(@PathVariable("id") Long id) {
        return orderService.payOrder(id);
    }

    /**
     * 获取用户在指定商店的已完成订单
     * @param shopId 商店ID
     * @return 已完成订单列表
     */
    @GetMapping("/user/completed")
    @ApiOperation("获取用户在指定商店的已完成订单")
    public Result getUserCompletedOrders(@RequestParam("shopId") Long shopId) {
        return orderService.getUserCompletedOrdersByShop(shopId);
    }
}
