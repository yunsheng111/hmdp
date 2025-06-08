package com.hmdp.dto;

import lombok.Data;

/**
 * <p>
 * 商家评论统计数据DTO
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
@Data
public class MerchantCommentStatisticsDTO {

    /**
     * 总评论数
     */
    private Long totalComments;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 评分分布（与前端格式完全匹配）
     */
    private RatingDistribution ratingDistribution;

    /**
     * 待回复数量（与前端字段名匹配）
     */
    private Long pendingReplyCount;

    /**
     * 回复率（小数形式，前端会乘以100显示）
     */
    private Double replyRate;

    /**
     * 评分分布内部类 - 直接匹配前端期望格式
     */
    @Data
    public static class RatingDistribution {
        private Long rating1 = 0L;
        private Long rating2 = 0L;
        private Long rating3 = 0L;
        private Long rating4 = 0L;
        private Long rating5 = 0L;
    }
}
