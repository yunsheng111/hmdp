package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户订单列表响应DTO
 *
 * @author yate
 * @since 2024-12-23
 */
@Data
public class UserOrderListDTO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品摘要
     */
    private String productSummary;

    /**
     * 订单总金额(分)
     */
    private Long totalAmount;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 商品数量
     */
    private Integer itemCount;
}
