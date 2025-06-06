package com.hmdp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品创建数据传输对象
 * 
 * {{CHENGQI:
 * Action: Added
 * Timestamp: [2024-07-30 16:00:00 +08:00]
 * Reason: 创建ProductCreateDTO用于接收商品创建请求数据，遵循P3-AR-001任务
 * Principle_Applied: KISS - 只包含创建商品所需的必要字段，避免不必要的复杂性
 * Principle_Applied: YAGNI - 只包含当前MVP所需字段，不预先添加未来可能需要的字段
 * }}
 */
@Data
public class ProductCreateDTO {
    /**
     * 商品标题
     */
    private String title;
    
    /**
     * 商品价格
     */
    private BigDecimal price;
    
    /**
     * 商品库存
     */
    private Integer stock;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品图片
     */
    private String images;
    
    /**
     * 所属店铺ID
     */
    private Long shopId;
    
    /**
     * 商品类型
     */
    private Integer type;

    /**
     * 商品规格列表，可选
     * {{CHENGQI:
     * Action: Added
     * Timestamp: [2024-12-19 14:32:00 +08:00]
     * Reason: 添加规格字段，支持在创建商品时同时创建规格信息，保持与更新接口的一致性
     * Principle_Applied: SRP - 一个接口完成一个完整的业务操作（商品+规格）
     * Principle_Applied: ACID - 保证商品和规格数据的一致性
     * }}
     */
    private List<ProductSpecDTO> specs;
}