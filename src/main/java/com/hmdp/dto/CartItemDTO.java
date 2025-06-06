package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项DTO
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class CartItemDTO {

    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * 购物车ID
     */
    private Long cartId;

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
     * 商品价格（分）
     */
    private Long productPrice;

    /**
     * 商品库存
     */
    private Integer productStock;

    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer productStatus;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品规格信息JSON
     */
    private String specifications;

    /**
     * 小计价格（分）
     */
    private Long subtotalPrice;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
