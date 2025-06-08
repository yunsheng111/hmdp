package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.dto.CreateOrderDTO;
import com.hmdp.dto.OrderCommentDTO;
import com.hmdp.dto.UserOrderQueryDTO;
import com.hmdp.entity.Order;

/**
 * <p>
 * 订单信息表 服务类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface IOrderService extends IService<Order> {

    /**
     * 从购物车创建订单
     * @param createOrderDTO 创建订单请求参数
     * @return 操作结果
     */
    Result createOrderFromCart(CreateOrderDTO createOrderDTO);

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    Result getOrderById(Long orderId);

    /**
     * 获取当前用户订单列表
     * @param status 订单状态（可选）
     * @param current 当前页
     * @param size 页大小
     * @return 订单列表
     */
    Result getCurrentUserOrders(Integer status, Integer current, Integer size);

    /**
     * 获取用户订单列表
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param current 当前页
     * @param size 页大小
     * @return 订单列表
     */
    Result getUserOrders(Long userId, Integer status, Integer current, Integer size);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态
     * @return 操作结果
     */
    Result updateOrderStatus(Long orderId, Integer status);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result cancelOrder(Long orderId);

    /**
     * 支付订单
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result payOrder(Long orderId);

    /**
     * 获取用户在指定商店的已完成订单
     * @param shopId 商店ID
     * @return 已完成订单列表
     */
    Result getUserCompletedOrdersByShop(Long shopId);

    // ========== 用户端订单管理方法 ==========

    /**
     * 查询用户订单列表
     * @param userId 用户ID
     * @param queryDTO 查询参数
     * @return 订单列表
     */
    Result getUserOrderList(Long userId, UserOrderQueryDTO queryDTO);

    /**
     * 查询用户订单详情
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    Result getUserOrderDetail(Long userId, Long orderId);

    /**
     * 用户订单支付
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result payUserOrder(Long userId, Long orderId);

    /**
     * 用户取消订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result cancelUserOrder(Long userId, Long orderId);

    /**
     * 用户确认收货
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result confirmUserOrder(Long userId, Long orderId);

    /**
     * 用户订单评价
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param commentDTO 评价内容
     * @return 操作结果
     */
    Result commentUserOrder(Long userId, Long orderId, OrderCommentDTO commentDTO);
}
