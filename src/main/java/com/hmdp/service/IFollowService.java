package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
