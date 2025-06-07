package com.hmdp.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品更新数据传输对象
 * 
 * {{CHENGQI:
 * Action: Added
 * Timestamp: [2024-07-30 16:05:00 +08:00]
 * Reason: 创建ProductUpdateDTO用于接收商品更新请求数据，遵循P3-AR-001任务
 * Principle_Applied: KISS - 只包含更新商品所需的必要字段，避免不必要的复杂性
 * Principle_Applied: YAGNI - 只包含当前MVP所需字段，不预先添加未来可能需要的字段
 * }}
 */
@Data
public class ProductUpdateDTO {
    /**
     * 商品ID，必填
     */
    private Long id;
    
    /**
     * 商品标题，可选
     */
    private String title;
    
    /**
     * 商品描述，可选
     */
    private String description;
    
    /**
     * 商品图片，多个以逗号分隔，可选
     */
    private String images;
    
    /**
     * 商品价格，单位为元，可选
     */
    private BigDecimal price;
    
    /**
     * 商品库存，可选
     */
    private Integer stock;
    
    /**
     * 商品分类ID，可选
     */
    private Long categoryId;

    /**
     * 商品状态：0-下架，1-上架，可选
     * {{CHENGQI:
     * Action: Added
     * Timestamp: [2024-07-31 10:15:00 +08:00]
     * Reason: 添加status字段，支持在更新商品时同时更新商品状态，解决编辑商品时状态无法保存的问题
     * Principle_Applied: KISS - 直接在DTO中添加必要字段，保持简单明了
     * }}
     */
    private Integer status;

    /**
     * 商品规格列表，可选
     * {{CHENGQI:
     * Action: Added
     * Timestamp: [2024-12-19 14:30:00 +08:00]
     * Reason: 添加规格字段，支持在更新商品时同时更新规格信息，避免分离的API调用
     * Principle_Applied: SRP - 一个接口完成一个完整的业务操作（商品+规格）
     * Principle_Applied: ACID - 保证商品和规格数据的一致性
     * }}
     */
    private List<ProductSpecDTO> specs;
}