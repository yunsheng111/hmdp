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
 * 商店评论表
 * </p>
 *
 * @author AI (Qitian Dasheng)
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop_comment")
public class ShopComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的商店ID
     */
    private Long shopId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 关联的订单ID（确保评论来自已验证的购买）
     */
    private Long orderId;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 商家回复内容
     */
    private String reply;

    /**
     * 状态：0=正常，1=用户隐藏，2=管理员隐藏
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}