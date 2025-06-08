package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.OrderCommentDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.dto.UserOrderQueryDTO;
import com.hmdp.service.IOrderService;
import com.hmdp.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户订单管理控制器
 *
 * @author yate
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/user/orders")
@Api(tags = "用户订单管理")
@Slf4j
public class UserOrderController {

    @Resource
    private IOrderService orderService;

    @GetMapping
    @ApiOperation("查询用户订单列表")
    public Result getUserOrderList(UserOrderQueryDTO queryDTO) {
        log.info("收到用户订单列表查询请求，参数：{}", queryDTO);

        // 获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            log.warn("用户未登录");
            return Result.fail("用户未登录");
        }

        log.info("当前用户ID：{}", user.getId());

        // 参数验证
        queryDTO.validate();
        
        // 强制设置用户ID为当前登录用户ID，确保只查询当前用户的订单
        // 忽略请求中可能传入的userId参数，防止越权访问
        queryDTO.setUserId(user.getId());
        log.info("设置当前用户ID后的参数：{}", queryDTO);

        // 调用服务层
        Result result = orderService.getUserOrderList(user.getId(), queryDTO);
        log.info("服务层返回结果：{}", result);
        return result;
    }

    @GetMapping("/{orderId}")
    @ApiOperation("查询订单详情")
    public Result getUserOrderDetail(@PathVariable Long orderId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }

        return orderService.getUserOrderDetail(user.getId(), orderId);
    }

    @PostMapping("/{orderId}/pay")
    @ApiOperation("订单支付")
    public Result payOrder(@PathVariable Long orderId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }

        return orderService.payUserOrder(user.getId(), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @ApiOperation("取消订单")
    public Result cancelOrder(@PathVariable Long orderId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }

        return orderService.cancelUserOrder(user.getId(), orderId);
    }

    @PostMapping("/{orderId}/confirm")
    @ApiOperation("确认收货")
    public Result confirmOrder(@PathVariable Long orderId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }

        return orderService.confirmUserOrder(user.getId(), orderId);
    }

    @PostMapping("/{orderId}/comment")
    @ApiOperation("订单评价")
    public Result commentOrder(@PathVariable Long orderId, 
                              @Valid @RequestBody OrderCommentDTO commentDTO) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }

        return orderService.commentUserOrder(user.getId(), orderId, commentDTO);
    }
}
