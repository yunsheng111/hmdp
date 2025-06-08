package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.dto.MerchantCommentQueryDTO;
import com.hmdp.dto.MerchantCommentReplyDTO;
import com.hmdp.dto.MerchantCommentReportDTO;

/**
 * <p>
 * 商家评论管理服务接口
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
public interface IMerchantCommentService {
    
    /**
     * 获取评论列表
     * 支持分页、筛选、排序
     *
     * @param queryDTO 查询参数
     * @return 评论列表结果
     */
    Result getCommentList(MerchantCommentQueryDTO queryDTO);
    
    /**
     * 商家回复评论
     *
     * @param commentId 评论ID
     * @param replyDTO 回复内容
     * @return 回复结果
     */
    Result replyComment(Long commentId, MerchantCommentReplyDTO replyDTO);
    
    /**
     * 修改商家回复
     *
     * @param commentId 评论ID
     * @param replyDTO 新的回复内容
     * @return 修改结果
     */
    Result updateReply(Long commentId, MerchantCommentReplyDTO replyDTO);
    
    /**
     * 删除商家回复
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    Result deleteReply(Long commentId);
    
    /**
     * 获取评论统计数据
     *
     * @return 统计数据结果
     */
    Result getStatistics();
    
    /**
     * 举报不当评论
     *
     * @param commentId 评论ID
     * @param reportDTO 举报信息
     * @return 举报结果
     */
    Result reportComment(Long commentId, MerchantCommentReportDTO reportDTO);
}
