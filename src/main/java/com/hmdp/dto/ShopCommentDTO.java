package com.hmdp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 商店评论数据传输对象
 * </p>
 *
 * @author AI (Qitian Dasheng)
 * @since 2024-07-29
 */
@Data
public class ShopCommentDTO {
    /**
     * 评论ID
     */
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
     * 关联的订单ID
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
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 用户头像URL
     */
    private String userIcon;
    
    /**
     * 是否是当前登录用户的评论
     */
    private Boolean isCurrentUserComment;
} 