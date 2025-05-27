package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    /**
     * 关注或取消关注指定用户
     * @param followUserId 关注或取消关注的用户ID
     * @param isFollow 是否关注，true表示关注，false表示取消关注
     * @return Result 返回结果对象
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //1.判断到底是关注还是取消关注
        if (isFollow) {
            //关注，
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(userId);
            this.save(follow);
        } else {
            //3.取消关注，删除数据 delete from tb_follow where user_id = ? and follow_user_id = ?
            this.remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
        }

        return Result.success();
    }


    /**
     * 判断当前用户是否关注了指定用户
     * @param followUserId 被关注的用户ID
     * @return Result 返回结果对象
     */
    @Override
    public Result isFollow(Long followUserId) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.查询是否关注
        Integer count = this.query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        //3.返回结果
        if (count > 0) {
            return Result.success(true);
        } else {
            return Result.success(false);
        }

    }
}
