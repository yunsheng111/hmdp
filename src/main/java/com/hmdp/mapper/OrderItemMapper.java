package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单项信息表 Mapper 接口
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

}
