package com.hmdp.dto;

import lombok.Data;
import java.util.List;

/**
 * 商品规格数据传输对象
 * 
 * Reason: 创建ProductSpecDTO用于传输商品规格数据
 * Principle_Applied: KISS - 设计简洁明了的DTO类，只包含必要字段
 * Principle_Applied: YAGNI - 仅添加当前需要的字段，不添加可能将来需要但目前不必要的字段
 * }}
 */
@Data
public class ProductSpecDTO {
    /**
     * 规格ID
     */
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 规格名称
     */
    private String name;
    
    /**
     * 规格值列表
     */
    private List<String> values;
    
    /**
     * 是否必选：0-否，1-是
     */
    private Integer required;
    
    /**
     * 排序值
     */
    private Integer sort;
} 