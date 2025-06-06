package com.hmdp.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品响应数据传输对象
 * Reason: 添加商品规格列表字段，以支持在查询商品时返回规格信息
 * Principle_Applied: KISS - 只添加必要的字段，不引入不必要的复杂性
 * Principle_Applied: DRY - 复用ProductSpecDTO作为列表元素类型
 * }}
 */
@Data
public class ProductResponseDTO {
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商铺ID
     */
    private Long shopId;
    
    /**
     * 商品分类ID
     */
    private Long categoryId;
    
    /**
     * 商品标题
     */
    private String title;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品图片，多个以逗号分隔
     */
    private String images;
    
    /**
     * 商品价格，单位为分
     */
    private Long price;
    
    /**
     * 商品库存
     */
    private Integer stock;
    
    /**
     * 商品销量
     */
    private Integer sold;
    
    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 商品规格列表
     * Reason: 添加商品规格列表字段，以支持在查询商品时返回规格信息
     * Principle_Applied: YAGNI - 仅添加当前需要的字段，不预先添加未来可能需要的字段
     * }}
     */
    private List<ProductSpecDTO> specs;
} 