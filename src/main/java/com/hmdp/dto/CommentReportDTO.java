package com.hmdp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 评论举报数据传输对象
 * </p>
 *
 * @author AI (Qitian Dasheng)
 * @since 2024-07-29
 */
@Data
public class CommentReportDTO {
    /**
     * 举报ID
     */
    private Long id;
    
    /**
     * 被举报的评论ID
     */
    private Long commentId;
    
    /**
     * 举报者ID（商家）
     */
    private Long reporterId;
    
    /**
     * 举报者名称
     */
    private String reporterName;
    
    /**
     * 举报原因
     */
    private String reason;
    
    /**
     * 状态：0=待处理，1=已处理
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 评论详情（嵌套对象）
     */
    private CommentInfoDTO comment;
    
    /**
     * 评论简要信息嵌套类
     */
    @Data
    public static class CommentInfoDTO {
        private Long id;
        private String content;
        private Integer rating;
        private Long shopId;
        private String shopName;
        private Long userId;
        private String userNickname;
    }
} 