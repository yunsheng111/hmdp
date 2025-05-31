package com.hmdp.service;

import java.util.List;

/**
 * <p>
 * 博客阅读状态服务接口
 * </p>
 *
 * @author yate
 * @since 2023-06-15
 */
public interface IBlogReadStatusService {

    /**
     * 标记博客为已读
     *
     * @param userId 用户ID
     * @param blogId 博客ID
     */
    void markBlogAsRead(Long userId, Long blogId);

    /**
     * 根据阅读状态查询博客ID列表
     *
     * @param userId 用户ID
     * @param authorId 作者ID
     * @param readStatus 阅读状态，可选值为 "ALL"(所有) 或 "UNREAD"(未读)
     * @param start 起始位置
     * @param end 结束位置
     * @return 符合条件的博客ID列表
     */
    List<Long> queryBlogIdsByReadStatus(Long userId, Long authorId, String readStatus, int start, int end);

    /**
     * 获取用户对特定作者的未读博客数量
     *
     * @param userId 用户ID
     * @param authorId 作者ID
     * @return 未读博客数量
     */
    int getUnreadCount(Long userId, Long authorId);

    /**
     * 发布博客后，更新粉丝的未读状态
     *
     * @param blogId 博客ID
     * @param authorId 作者ID
     * @param followerIds 粉丝ID列表
     */
    void updateUnreadStatusAfterPublish(Long blogId, Long authorId, List<Long> followerIds);
} 