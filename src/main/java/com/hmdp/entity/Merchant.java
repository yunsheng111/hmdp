package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商家信息表
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_merchant")
public class Merchant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商家名称
     */
    private String name;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码，加密存储
     */
    private String password;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 商家描述
     */
    private String description;

    /**
     * 商户分类ID
     */
    private Long typeId;

    /**
     * 商户分类名称（用于显示，不存储到数据库）
     */
    @TableField(exist = false)
    private String typeName;

    /**
     * 商家状态：0-待审核，1-审核通过，2-审核拒绝
     */
    private Integer status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 