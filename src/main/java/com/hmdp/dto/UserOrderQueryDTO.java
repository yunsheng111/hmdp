package com.hmdp.dto;

import lombok.Data;

/**
 * 用户订单查询请求DTO
 *
 * @author yate
 * @since 2024-12-23
 */
@Data
public class UserOrderQueryDTO {

    /**
     * 用户ID，用于限制只查询特定用户的订单
     */
    private Long userId;

    /**
     * 订单状态筛选 (1-待付款,2-已付款,3-已完成,4-已取消,5-已退款)
     */
    private Integer status;

    /**
     * 开始日期 (yyyy-MM-dd格式)
     */
    private String startDate;

    /**
     * 结束日期 (yyyy-MM-dd格式)
     */
    private String endDate;

    /**
     * 页码，默认1
     */
    private Integer current = 1;

    /**
     * 每页数量，默认10
     */
    private Integer size = 10;

    /**
     * 参数验证方法
     */
    public void validate() {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 10;
        }
    }
}
