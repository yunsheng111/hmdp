package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应DTO
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class OrderDTO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商铺ID
     */
    private Long shopId;

    /**
     * 商铺名称
     */
    private String shopName;

    /**
     * 订单总金额，单位为分
     */
    private Long totalAmount;

    /**
     * 订单状态：1-待支付，2-已支付，3-已完成，4-已取消，5-已退款
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 支付方式：1-余额支付，2-支付宝，3-微信
     */
    private Integer payType;

    /**
     * 支付方式描述
     */
    private String payTypeDesc;

    /**
     * 配送地址
     */
    private String address;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 订单项列表
     */
    private List<OrderItemDTO> items;
}
