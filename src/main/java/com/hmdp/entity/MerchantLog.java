package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商家操作日志表
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_merchant_log")
public class MerchantLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 操作类型：1-登录，2-新增，3-修改，4-删除，5-查询
     */
    private Integer type;

    /**
     * 操作模块：1-商家账户，2-店铺，3-商品，4-优惠券，5-订单
     */
    private Integer module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 操作结果：0-失败，1-成功
     */
    private Integer result;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 