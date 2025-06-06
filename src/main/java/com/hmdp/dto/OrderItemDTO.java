package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单项DTO
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class OrderItemDTO {

    /**
     * 订单项ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品单价，单位为分
     */
    private Long productPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 商品规格信息JSON
     */
    private String specifications;

    /**
     * 小计金额，单位为分
     */
    private Long subtotalAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
