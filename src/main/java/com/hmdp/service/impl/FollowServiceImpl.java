package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.FollowCountDTO;
import com.hmdp.dto.FollowUserDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;
    @Resource
    private IUserInfoService userInfoService;

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
        String key = "follows:" + userId;
        //1.判断到底是关注还是取消关注
        if (isFollow) {
            //关注，
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(userId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                //关注成功，把关注的用户ID放入Redis缓存  sadd userid followUserid
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            //3.取消关注，删除数据 delete from tb_follow where user_id = ? and follow_user_id = ?
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
            if (isSuccess) {
                //取消关注成功，把关注的用户ID从Redis缓存中移除  srem userid followUserid
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
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

    @Override
    public Result followCommons(Long id) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        //2.求出共同关注的用户
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        //3.判断是否有共同关注的用户
        if (intersect == null || intersect.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        //4.解析出共同关注的用户ID
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());

        //5.查询用户
        List<UserDTO> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        return Result.success(users);
    }

    @Override
    public Result queryFollowees(Long userId) {
        // 1. 查询当前用户关注的所有用户ID
        List<Follow> follows = query().eq("user_id", userId).list();
        if (follows == null || follows.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Long> followUserIds = follows.stream().map(Follow::getFollowUserId).collect(Collectors.toList());

        // 2. 查询这些被关注用户的基本信息 (User)
        List<User> userList = userService.listByIds(followUserIds);
        if (userList == null || userList.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 3. 查询这些被关注用户的详细信息 (UserInfo for introduce)
        List<UserInfo> userInfoList = userInfoService.listByIds(followUserIds);
        Map<Long, String> userInfoMap = userInfoList.stream()
                .collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getIntroduce, (oldValue, newValue) -> newValue)); // 处理可能的重复userId，尽管不应该发生

        // 4. 组装成FollowUserDTO
        List<FollowUserDTO> followUserDTOs = userList.stream().map(user -> {
            FollowUserDTO dto = BeanUtil.copyProperties(user, FollowUserDTO.class);
            dto.setIntroduce(userInfoMap.get(user.getId()));
            return dto;
        }).collect(Collectors.toList());

        return Result.success(followUserDTOs);
    }

    @Override
    public Result queryFollowCount(Long userId) {
        // 直接从tb_user_info表中读取，如果该表数据准确的话
        UserInfo userInfo = userInfoService.getById(userId);
        FollowCountDTO countDTO = new FollowCountDTO();
        if (userInfo != null && userInfo.getFollowee() != null) {
            countDTO.setFollowee(userInfo.getFollowee());
        } else {
            // 如果UserInfo中没有或者不准确，再从follow表统计 (备选方案)
            // int count = query().eq("user_id", userId).count();
            // countDTO.setFollowee(count > 0 ? count : 0);
            // 为简单起见，如果前端需要实时准确，且UserInfo.followee可能延迟，则应直接count查询
            // 这里我们先假设UserInfo.followee是可靠的，或者业务上允许一定的延迟
            // 如果需要强一致性，应该直接count tb_follow表
            // 考虑到前端显示的是 (关注的人数)，我们从tb_follow统计更准确
            int count = Math.toIntExact(query().eq("user_id", userId).count());
            countDTO.setFollowee(count);
        }
        return Result.success(countDTO);
    }
}
