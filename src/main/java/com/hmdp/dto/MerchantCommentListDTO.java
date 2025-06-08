package com.hmdp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 商家评论列表DTO
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
@Data
public class MerchantCommentListDTO {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 用户头像
     */
    private String userIcon;
    
    /**
     * 订单ID
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
     * 回复时间
     */
    private LocalDateTime replyTime;
}
