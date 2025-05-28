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
 * 商家资质信息表
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_merchant_qualification")
public class MerchantQualification implements Serializable {

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
     * 营业执照图片路径
     */
    private String businessLicense;

    /**
     * 法人身份证图片路径
     */
    private String idCard;

    /**
     * 经营许可证图片路径
     */
    private String businessPermit;

    /**
     * 餐饮服务许可证图片路径
     */
    private String foodServicePermit;

    /**
     * 其他资质证明
     */
    private String otherQualifications;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核拒绝
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String comment;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 