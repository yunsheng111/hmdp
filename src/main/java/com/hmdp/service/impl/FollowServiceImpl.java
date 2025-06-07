package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.AuthorOptionDTO;
import com.hmdp.dto.EnhancedFollowUserDTO;
import com.hmdp.dto.FollowCountDTO;
import com.hmdp.dto.FollowUserDTO;
import com.hmdp.dto.PagedFollowResultDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.AUTHOR_UNREAD_COUNT_KEY_PREFIX;

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

    private static final Logger log = LoggerFactory.getLogger(FollowServiceImpl.class);

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
        FollowCountDTO countDTO = new FollowCountDTO();
        
        // 查询关注数
        int followeeCount = Math.toIntExact(query().eq("user_id", userId).count());
        countDTO.setFollowee(followeeCount);
        
        // 查询粉丝数
        int followerCount = Math.toIntExact(query().eq("follow_user_id", userId).count());
        countDTO.setFollower(followerCount);
        
        return Result.success(countDTO);
    }

    @Override
    public Result queryFollowers(Long userId) {
        log.info("开始查询用户的粉丝列表，userId={}", userId);
        
        // 1. 查询关注了当前用户的所有用户ID (粉丝)
        List<Follow> followers = query().eq("follow_user_id", userId).list();
        if (followers == null || followers.isEmpty()) {
            log.info("用户{}没有粉丝", userId);
            return Result.success(Collections.emptyList());
        }
        List<Long> followerUserIds = followers.stream().map(Follow::getUserId).collect(Collectors.toList());
        log.info("用户{}有{}个粉丝", userId, followerUserIds.size());

        // 2. 查询这些粉丝的基本信息 (User)
        List<User> userList = userService.listByIds(followerUserIds);
        if (userList == null || userList.isEmpty()) {
            log.warn("未找到粉丝的基本信息，userId={}", userId);
            return Result.success(Collections.emptyList());
        }

        // 3. 查询这些粉丝的详细信息 (UserInfo for introduce)
        List<UserInfo> userInfoList = userInfoService.listByIds(followerUserIds);
        Map<Long, String> userInfoMap = userInfoList.stream()
                .collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getIntroduce, (oldValue, newValue) -> newValue));

        // 4. 组装成FollowUserDTO
        List<FollowUserDTO> followerDTOs = userList.stream().map(user -> {
            FollowUserDTO dto = BeanUtil.copyProperties(user, FollowUserDTO.class);
            dto.setIntroduce(userInfoMap.get(user.getId()));
            return dto;
        }).collect(Collectors.toList());

        return Result.success(followerDTOs);
    }

    @Override
    public List<AuthorOptionDTO> getFollowedAuthorsWithUnreadCount(Long userId) {
        log.info("开始获取用户关注的作者列表及未读数，userId={}", userId);

        try {
            // 1. 查询用户关注的所有用户
            List<Follow> follows = query().eq("user_id", userId).list();
            if (follows == null || follows.isEmpty()) {
                log.info("用户{}没有关注任何人", userId);
                return Collections.emptyList();
            }

            // 2. 获取关注的用户ID列表
            List<Long> followUserIds = follows.stream()
                    .map(Follow::getFollowUserId)
                    .collect(Collectors.toList());
            log.info("用户{}关注了{}个用户", userId, followUserIds.size());

            // 3. 查询这些用户的基本信息
            List<User> users = userService.listByIds(followUserIds);
            if (users == null || users.isEmpty()) {
                log.warn("未找到关注用户的基本信息，userId={}", userId);
                return Collections.emptyList();
            }

            // 4. 组装作者选项列表
            List<AuthorOptionDTO> authorOptions = users.stream().map(user -> {
                AuthorOptionDTO dto = new AuthorOptionDTO();
                dto.setAuthorId(user.getId());
                dto.setAuthorName(user.getNickName());
                dto.setAuthorIcon(user.getIcon());

                // 5. 从Redis获取该作者的未读博客数
                String authorUnreadKey = AUTHOR_UNREAD_COUNT_KEY_PREFIX + userId + ":" + user.getId();
                String unreadCountStr = stringRedisTemplate.opsForValue().get(authorUnreadKey);
                int unreadCount = 0;
                if (unreadCountStr != null && !unreadCountStr.isEmpty()) {
                    try {
                        unreadCount = Integer.parseInt(unreadCountStr);
                    } catch (NumberFormatException e) {
                        log.warn("解析未读数失败，userId={}，authorId={}，unreadCountStr={}",
                                userId, user.getId(), unreadCountStr);
                    }
                }
                dto.setUnreadCount(unreadCount);

                return dto;
            }).collect(Collectors.toList());

            log.info("成功获取用户{}的关注作者列表，共{}个作者", userId, authorOptions.size());
            return authorOptions;

        } catch (Exception e) {
            log.error("获取关注作者列表时发生错误，userId={}，错误：{}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Result queryFolloweesPaged(Long userId, Integer page, Integer size) {
        log.info("开始分页查询用户关注列表，userId={}，page={}，size={}", userId, page, size);

        try {
            // 1. 创建分页对象
            Page<Follow> followPage = new Page<>(page, size);

            // 2. 分页查询关注关系
            IPage<Follow> followIPage = this.page(followPage,
                new QueryWrapper<Follow>().eq("user_id", userId));

            if (followIPage.getRecords().isEmpty()) {
                log.info("用户{}没有关注任何人", userId);
                return Result.success(new PagedFollowResultDTO<>(Collections.emptyList(), 0L, page, size));
            }

            // 3. 获取关注的用户ID列表
            List<Long> followUserIds = followIPage.getRecords().stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toList());

            // 4. 查询用户基本信息
            List<User> userList = userService.listByIds(followUserIds);
            if (userList.isEmpty()) {
                log.warn("未找到关注用户的基本信息，userId={}", userId);
                return Result.success(new PagedFollowResultDTO<>(Collections.emptyList(), 0L, page, size));
            }

            // 5. 查询用户详细信息
            List<UserInfo> userInfoList = userInfoService.listByIds(followUserIds);
            Map<Long, String> userInfoMap = userInfoList.stream()
                .collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getIntroduce, (oldValue, newValue) -> newValue));

            // 6. 获取当前登录用户ID，用于判断关注状态
            Long currentUserId = UserHolder.getUser().getId();

            // 7. 组装增强的用户DTO
            List<EnhancedFollowUserDTO> enhancedDTOs = userList.stream().map(user -> {
                EnhancedFollowUserDTO dto = EnhancedFollowUserDTO.fromFollowUserDTO(
                    createFollowUserDTO(user, userInfoMap.get(user.getId())));

                // 设置关注状态（在关注列表中，当前用户肯定已关注这些用户）
                dto.setIsFollowed(true);

                // 检查是否互相关注
                boolean isMutualFollow = checkMutualFollow(currentUserId, user.getId());
                dto.setIsMutualFollow(isMutualFollow);

                return dto;
            }).collect(Collectors.toList());

            // 8. 创建分页结果
            PagedFollowResultDTO<EnhancedFollowUserDTO> result = new PagedFollowResultDTO<>(
                enhancedDTOs, followIPage.getTotal(), page, size);

            log.info("成功分页查询用户{}的关注列表，共{}条记录", userId, followIPage.getTotal());
            return Result.success(result);

        } catch (Exception e) {
            log.error("分页查询关注列表时发生错误，userId={}，错误：{}", userId, e.getMessage(), e);
            return Result.fail("查询关注列表失败");
        }
    }

    @Override
    public Result queryFollowersPaged(Long userId, Integer page, Integer size) {
        log.info("开始分页查询用户粉丝列表，userId={}，page={}，size={}", userId, page, size);

        try {
            // 1. 创建分页对象
            Page<Follow> followPage = new Page<>(page, size);

            // 2. 分页查询粉丝关系
            IPage<Follow> followIPage = this.page(followPage,
                new QueryWrapper<Follow>().eq("follow_user_id", userId));

            if (followIPage.getRecords().isEmpty()) {
                log.info("用户{}没有粉丝", userId);
                return Result.success(new PagedFollowResultDTO<>(Collections.emptyList(), 0L, page, size));
            }

            // 3. 获取粉丝的用户ID列表
            List<Long> followerUserIds = followIPage.getRecords().stream()
                .map(Follow::getUserId)
                .collect(Collectors.toList());

            // 4. 查询粉丝基本信息
            List<User> userList = userService.listByIds(followerUserIds);
            if (userList.isEmpty()) {
                log.warn("未找到粉丝的基本信息，userId={}", userId);
                return Result.success(new PagedFollowResultDTO<>(Collections.emptyList(), 0L, page, size));
            }

            // 5. 查询粉丝详细信息
            List<UserInfo> userInfoList = userInfoService.listByIds(followerUserIds);
            Map<Long, String> userInfoMap = userInfoList.stream()
                .collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getIntroduce, (oldValue, newValue) -> newValue));

            // 6. 获取当前登录用户ID，用于判断关注状态
            Long currentUserId = UserHolder.getUser().getId();

            // 7. 批量查询当前用户对这些粉丝的关注状态
            Map<Long, Boolean> followStatusMap = batchCheckFollowStatus(currentUserId, followerUserIds);

            // 8. 组装增强的用户DTO
            List<EnhancedFollowUserDTO> enhancedDTOs = userList.stream().map(user -> {
                EnhancedFollowUserDTO dto = EnhancedFollowUserDTO.fromFollowUserDTO(
                    createFollowUserDTO(user, userInfoMap.get(user.getId())));

                // 设置关注状态
                boolean isFollowed = followStatusMap.getOrDefault(user.getId(), false);
                dto.setIsFollowed(isFollowed);

                // 检查是否互相关注（粉丝关注了当前用户，如果当前用户也关注了粉丝，则为互关）
                dto.setIsMutualFollow(isFollowed);

                return dto;
            }).collect(Collectors.toList());

            // 9. 创建分页结果
            PagedFollowResultDTO<EnhancedFollowUserDTO> result = new PagedFollowResultDTO<>(
                enhancedDTOs, followIPage.getTotal(), page, size);

            log.info("成功分页查询用户{}的粉丝列表，共{}条记录", userId, followIPage.getTotal());
            return Result.success(result);

        } catch (Exception e) {
            log.error("分页查询粉丝列表时发生错误，userId={}，错误：{}", userId, e.getMessage(), e);
            return Result.fail("查询粉丝列表失败");
        }
    }

    @Override
    public Result batchQueryFollowStatus(List<Long> targetUserIds) {
        log.info("开始批量查询关注状态，目标用户数量：{}", targetUserIds.size());

        try {
            // 1. 获取当前登录用户
            Long currentUserId = UserHolder.getUser().getId();

            // 2. 批量查询关注状态
            Map<Long, Boolean> followStatusMap = batchCheckFollowStatus(currentUserId, targetUserIds);

            log.info("成功批量查询关注状态，用户{}对{}个用户的关注状态", currentUserId, targetUserIds.size());
            return Result.success(followStatusMap);

        } catch (Exception e) {
            log.error("批量查询关注状态时发生错误，错误：{}", e.getMessage(), e);
            return Result.fail("批量查询关注状态失败");
        }
    }

    /**
     * 批量检查关注状态
     * @param userId 当前用户ID
     * @param targetUserIds 目标用户ID列表
     * @return 关注状态映射
     */
    private Map<Long, Boolean> batchCheckFollowStatus(Long userId, List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return new HashMap<>();
        }

        // 查询当前用户关注的所有目标用户
        List<Follow> follows = this.query()
            .eq("user_id", userId)
            .in("follow_user_id", targetUserIds)
            .list();

        // 构建关注状态映射
        Set<Long> followedUserIds = follows.stream()
            .map(Follow::getFollowUserId)
            .collect(Collectors.toSet());

        Map<Long, Boolean> statusMap = new HashMap<>();
        for (Long targetUserId : targetUserIds) {
            statusMap.put(targetUserId, followedUserIds.contains(targetUserId));
        }

        return statusMap;
    }

    /**
     * 检查两个用户是否互相关注
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 是否互相关注
     */
    private boolean checkMutualFollow(Long userId1, Long userId2) {
        // 检查用户1是否关注用户2
        boolean user1FollowsUser2 = this.query()
            .eq("user_id", userId1)
            .eq("follow_user_id", userId2)
            .count() > 0;

        if (!user1FollowsUser2) {
            return false;
        }

        // 检查用户2是否关注用户1
        boolean user2FollowsUser1 = this.query()
            .eq("user_id", userId2)
            .eq("follow_user_id", userId1)
            .count() > 0;

        return user2FollowsUser1;
    }

    /**
     * 创建FollowUserDTO
     * @param user 用户实体
     * @param introduce 用户简介
     * @return FollowUserDTO
     */
    private FollowUserDTO createFollowUserDTO(User user, String introduce) {
        FollowUserDTO dto = BeanUtil.copyProperties(user, FollowUserDTO.class);
        dto.setIntroduce(introduce);
        return dto;
    }
}
