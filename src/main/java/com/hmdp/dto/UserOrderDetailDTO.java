package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户订单详情响应DTO
 *
 * @author yate
 * @since 2024-12-23
 */
@Data
public class UserOrderDetailDTO {

    // 订单基本信息
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
     * 订单总金额
     */
    private Long totalAmount;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 配送地址
     */
    private String address;

    // 时间信息
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    // 订单项列表
    /**
     * 订单项列表
     */
    private List<OrderItemDTO> items;

    // 金额明细
    /**
     * 商品金额
     */
    private Long itemsAmount;

    /**
     * 优惠金额
     */
    private Long discountAmount;

    /**
     * 运费
     */
    private Long deliveryFee;

    /**
     * 实付金额
     */
    private Long actualAmount;
}
