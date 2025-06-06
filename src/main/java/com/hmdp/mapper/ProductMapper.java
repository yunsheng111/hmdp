package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品数据访问接口
 * 
 * {{CHENGQI:
 * Action: Added
 * Timestamp: [2024-07-30 16:20:00 +08:00]
 * Reason: 创建ProductMapper接口用于数据库操作，遵循P3-LD-001任务
 * Principle_Applied: KISS - 简单直接地继承BaseMapper，不添加不必要的复杂性
 * Principle_Applied: YAGNI - 当前MVP阶段不需要额外的自定义方法，使用BaseMapper提供的基础方法即可满足需求
 * }}
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // 使用MyBatis-Plus提供的BaseMapper方法即可满足MVP需求
    // 如有更复杂的查询需求，可在此添加自定义方法

    /**
     * 扣减商品库存
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    @Update("UPDATE tb_product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    boolean updateStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 恢复商品库存
     * @param productId 商品ID
     * @param quantity 恢复数量
     * @return 是否成功
     */
    @Update("UPDATE tb_product SET stock = stock + #{quantity} WHERE id = #{productId}")
    boolean restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}