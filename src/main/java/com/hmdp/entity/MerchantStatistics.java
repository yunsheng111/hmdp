package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 商家统计数据表
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_merchant_statistics")
public class MerchantStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商铺ID
     */
    private Long shopId;

    /**
     * 统计日期
     */
    private LocalDate statisticsDate;

    /**
     * 销售额，单位为分
     */
    private Long salesAmount;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 新用户数量
     */
    private Integer newUserCount;

    /**
     * 活跃用户数量
     */
    private Integer activeUserCount;

    /**
     * 访问量（PV）
     */
    private Integer pageViews;

    /**
     * 访客数（UV）
     */
    private Integer uniqueVisitors;

    /**
     * 转化率（百分比）
     */
    private Double conversionRate;

    /**
     * 统计类型：1-日报，2-周报，3-月报
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 