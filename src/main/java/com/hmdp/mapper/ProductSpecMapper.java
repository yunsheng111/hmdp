package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.ProductSpec;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品规格数据访问接口
 * {{CHENGQI:
 * Reason: 创建商品规格Mapper接口，支持根据商品ID查询规格列表
 * Principle_Applied: KISS - 仅创建必要的接口方法，保持简单
 * Principle_Applied: DRY - 继承BaseMapper以复用通用CRUD方法
 * Principle_Applied: SRP - 接口只负责商品规格数据访问，符合单一职责
 * }}
 */
@Mapper
public interface ProductSpecMapper extends BaseMapper<ProductSpec> {
    
    /**
     * 根据商品ID查询规格列表
     * 
     * @param productId 商品ID
     * @return 规格列表
     */
    List<ProductSpec> findByProductId(@Param("productId") Long productId);
} 