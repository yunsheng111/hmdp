package com.hmdp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品查询DTO
 */
@Data
public class ProductQueryDTO {
    /**
     * 店铺ID
     */
    private Long shopId;
    
    /**
     * 商品标题（模糊查询）
     */
    private String title;
    
    /**
     * 商品状态（0-下架，1-上架）
     */
    private Integer status;
    
    /**
     * 最低价格
     */
    private BigDecimal minPrice;
    
    /**
     * 最高价格
     */
    private BigDecimal maxPrice;
    
    /**
     * 当前页码，默认为1
     */
    private Integer current = 1;

    /**
     * 每页大小，默认为10
     */
    private Integer size = 10;
} 