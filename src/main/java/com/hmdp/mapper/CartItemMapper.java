package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.dto.CartItemDTO;
import com.hmdp.entity.CartItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 购物车项表 Mapper 接口
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * 根据购物车ID查询购物车项详情
     * @param cartId 购物车ID
     * @return 购物车项详情列表
     */
    @Select("SELECT " +
            "ci.id, ci.cart_id, ci.product_id, ci.quantity, ci.specifications, ci.create_time, " +
            "p.title as product_title, p.images as product_image, p.price as product_price, " +
            "p.stock as product_stock, p.status as product_status, " +
            "(ci.quantity * p.price) as subtotal_price " +
            "FROM tb_cart_item ci " +
            "LEFT JOIN tb_product p ON ci.product_id = p.id " +
            "WHERE ci.cart_id = #{cartId} " +
            "ORDER BY ci.create_time DESC")
    List<CartItemDTO> selectCartItemsWithProduct(@Param("cartId") Long cartId);
}
