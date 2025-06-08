package com.hmdp.dto;

import lombok.Data;

/**
 * <p>
 * 商家评论查询参数DTO
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
@Data
public class MerchantCommentQueryDTO {
    
    /**
     * 页码，默认第1页
     */
    private Integer current = 1;
    
    /**
     * 每页大小，默认10条
     */
    private Integer size = 10;
    
    /**
     * 按评分筛选(1-5)
     */
    private Integer rating;
    
    /**
     * 按状态筛选(0-正常,1-用户隐藏,2-管理员隐藏)
     */
    private Integer status;
    
    /**
     * 是否已回复筛选
     */
    private Boolean hasReply;
    
    /**
     * 排序字段，默认按时间排序
     * 可选值："time"、"rating"
     */
    private String sortBy = "time";
    
    /**
     * 排序方向，默认降序
     * 可选值："asc"、"desc"
     */
    private String order = "desc";

    /**
     * 店铺ID，用于筛选特定店铺的评论
     * 如果为null，则查询商家所有店铺的评论
     */
    private Long shopId;

    /**
     * 验证并设置默认值
     */
    public void validate() {
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 10;
        }
        if (rating != null && (rating < 1 || rating > 5)) {
            rating = null;
        }
        if (status != null && (status < 0 || status > 2)) {
            status = null;
        }
        if (sortBy == null || (!sortBy.equals("time") && !sortBy.equals("rating"))) {
            sortBy = "time";
        }
        if (order == null || (!order.equals("asc") && !order.equals("desc"))) {
            order = "desc";
        }
    }
}
