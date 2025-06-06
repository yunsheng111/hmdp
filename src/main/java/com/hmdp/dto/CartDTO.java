package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车响应DTO
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class CartDTO {

    /**
     * 购物车ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 购物车项列表
     */
    private List<CartItemDTO> items;

    /**
     * 购物车总价（分）
     */
    private Long totalPrice;

    /**
     * 购物车总数量
     */
    private Integer totalQuantity;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
