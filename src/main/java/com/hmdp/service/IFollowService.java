package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.dto.AuthorOptionDTO;
import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface IFollowService extends IService<Follow> {
    /**
     * 关注或取消关注用户
     *
     * @param followUserId 被关注用户的ID
     * @param isFollow 是否关注，true表示关注，false表示取消关注
     * @return 操作结果
     */
    Result follow(Long followUserId, Boolean isFollow);

    /**
     * 检查当前用户是否关注了指定用户
     *
     * @param followUserId 要检查是否关注的用户ID
     * @return 如果当前用户关注了指定用户，则返回true；否则返回false
     */
    Result isFollow(Long followUserId);

    Result followCommons(Long id);

    Result queryFollowees(Long userId);

    Result queryFollowCount(Long userId);
    
    /**
     * 查询用户的粉丝列表
     *
     * @param userId 用户ID
     * @return 粉丝列表
     */
    Result queryFollowers(Long userId);

    /**
     * 获取用户关注的作者列表及其未读博客数
     *
     * @param userId 用户ID
     * @return 关注的作者列表
     */
    List<AuthorOptionDTO> getFollowedAuthorsWithUnreadCount(Long userId);

    /**
     * 分页查询用户关注的人列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页关注列表
     */
    Result queryFolloweesPaged(Long userId, Integer page, Integer size);

    /**
     * 分页查询用户的粉丝列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页粉丝列表
     */
    Result queryFollowersPaged(Long userId, Integer page, Integer size);

    /**
     * 批量查询关注状态
     *
     * @param targetUserIds 目标用户ID列表
     * @return 关注状态映射
     */
    Result batchQueryFollowStatus(List<Long> targetUserIds);
}
