package com.hmdp.service.impl;

import com.hmdp.entity.Blog;
import com.hmdp.service.IBlogReadStatusService;
import com.hmdp.service.IBlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 博客阅读状态服务实现类
 * </p>
 *
 * @author yate
 * @since 2023-06-15
 */
@Service
@Slf4j
public class BlogReadStatusServiceImpl implements IBlogReadStatusService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    // 删除字段级注入
    // @Resource
    // private IBlogService blogService;
    
    // 声明但不初始化
    private IBlogService blogService;
    
    // 通过setter方法注入，并使用@Lazy注解延迟初始化
    @Autowired
    public void setBlogService(@Lazy IBlogService blogService) {
        this.blogService = blogService;
    }
    
    @Override
    public void markBlogAsRead(Long userId, Long blogId) {
        log.info("标记博客为已读：userId={}，blogId={}", userId, blogId);
        
        // 1. 获取博客信息
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            log.warn("博客不存在：blogId={}", blogId);
            throw new RuntimeException("博客不存在");
        }
        
        Long authorId = blog.getUserId();
        
        // 2. 添加到用户已读集合
        String readKey = USER_READ_BLOGS_KEY + userId;
        stringRedisTemplate.opsForSet().add(readKey, blogId.toString());
        
        // 3. 从用户未读集合中移除
        String unreadKey = USER_UNREAD_BLOGS_KEY + userId + ":" + authorId;
        stringRedisTemplate.opsForSet().remove(unreadKey, blogId.toString());
        
        // 4. 更新未读计数
        String countKey = USER_UNREAD_COUNT_KEY + userId;
        // 获取当前未读数
        Object countObj = stringRedisTemplate.opsForHash().get(countKey, authorId.toString());
        String countStr = countObj == null ? null : countObj.toString();
        int count = 0;
        
        try {
            if (countStr != null && !countStr.isEmpty()) {
                count = Integer.parseInt(countStr);
                log.debug("获取到用户[{}]对作者[{}]的未读计数: {}", userId, authorId, count);
            } else {
                log.debug("用户[{}]对作者[{}]没有未读计数记录", userId, authorId);
            }
            
            if (count > 0) {
                // 减少计数
                stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), String.valueOf(count - 1));
                log.debug("更新用户[{}]对作者[{}]的未读计数: {} -> {}", userId, authorId, count, count - 1);
            }
        } catch (NumberFormatException e) {
            log.error("解析未读计数时出错，用户ID={}，作者ID={}，值={}，错误: {}", 
                    userId, authorId, countStr, e.getMessage());
            // 重置计数为0
            stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), "0");
        }
        
        // 5. 设置过期时间
        stringRedisTemplate.expire(readKey, USER_READ_TTL, TimeUnit.DAYS);
        stringRedisTemplate.expire(unreadKey, USER_UNREAD_TTL, TimeUnit.DAYS);
        stringRedisTemplate.expire(countKey, USER_UNREAD_TTL, TimeUnit.DAYS);
        
        log.info("博客已标记为已读：userId={}，blogId={}，authorId={}", userId, blogId, authorId);
    }
    
    @Override
    public List<Long> queryBlogIdsByReadStatus(Long userId, Long authorId, String readStatus, int start, int end) {
        log.info("查询博客ID列表：userId={}，authorId={}，readStatus={}，start={}，end={}", userId, authorId, readStatus, start, end);
        
        // 判断是查询所有还是仅查询未读
        boolean queryUnreadOnly = "UNREAD".equals(readStatus);
        
        if (queryUnreadOnly) {
            // 直接查询未读博客ID
            String unreadKey = USER_UNREAD_BLOGS_KEY + userId + ":" + authorId;
            Set<String> unreadIds = stringRedisTemplate.opsForSet().members(unreadKey);
            
            if (unreadIds == null || unreadIds.isEmpty()) {
                log.info("未找到未读博客：userId={}，authorId={}", userId, authorId);
                return Collections.emptyList();
            }
            
            // 转换并排序(按ID排序，实际应用中可改进为按时间排序)
            List<Long> blogIds = unreadIds.stream()
                    .map(Long::valueOf)
                    .sorted((a, b) -> b.compareTo(a)) // 降序排列
                    .collect(Collectors.toList());
            
            // 分页处理
            int toIndex = Math.min(end, blogIds.size());
            if (start >= blogIds.size() || start >= toIndex) {
                return Collections.emptyList();
            }
            
            List<Long> result = blogIds.subList(start, toIndex);
            log.info("查询到未读博客ID列表：count={}", result.size());
            return result;
        } else {
            // 查询作者的所有博客
            String authorKey = AUTHOR_BLOGS_KEY + authorId;
            Set<String> blogIdSet = stringRedisTemplate.opsForSet().members(authorKey);
            
            if (blogIdSet == null || blogIdSet.isEmpty()) {
                // 如果Redis中没有，则从数据库查询并写入Redis
                log.info("Redis中未找到作者博客列表，从数据库查询：authorId={}", authorId);
                List<Blog> blogs = blogService.query().eq("user_id", authorId).list();
                if (blogs.isEmpty()) {
                    log.info("作者没有博客：authorId={}", authorId);
                    return Collections.emptyList();
                }
                
                // 将博客ID添加到作者博客集合
                blogIdSet = new HashSet<>();
                for (Blog blog : blogs) {
                    blogIdSet.add(blog.getId().toString());
                    stringRedisTemplate.opsForSet().add(authorKey, blog.getId().toString());
                }
                
                // 设置过期时间
                stringRedisTemplate.expire(authorKey, BLOG_INFO_TTL, TimeUnit.DAYS);
            }
            
            // 转换并排序(按ID降序排列，实际应用中可改进为按时间排序)
            List<Long> blogIds = blogIdSet.stream()
                    .map(Long::valueOf)
                    .sorted((a, b) -> b.compareTo(a)) // 降序排列
                    .collect(Collectors.toList());
            
            // 分页处理
            int toIndex = Math.min(end, blogIds.size());
            if (start >= blogIds.size() || start >= toIndex) {
                return Collections.emptyList();
            }
            
            List<Long> result = blogIds.subList(start, toIndex);
            log.info("查询到作者博客ID列表：authorId={}，count={}", authorId, result.size());
            return result;
        }
    }
    
    @Override
    public int getUnreadCount(Long userId, Long authorId) {
        try {
            String countKey = USER_UNREAD_COUNT_KEY + userId;
            Object countObj = stringRedisTemplate.opsForHash().get(countKey, authorId.toString());
            
            if (countObj == null) {
                log.debug("用户[{}]对作者[{}]没有未读计数记录，返回0", userId, authorId);
                return 0;
            }
            
            String countStr = countObj.toString();
            if (countStr.isEmpty()) {
                log.debug("用户[{}]对作者[{}]的未读计数记录为空字符串，返回0", userId, authorId);
                return 0;
            }
            
            int count = Integer.parseInt(countStr);
            log.debug("获取到用户[{}]对作者[{}]的未读计数: {}", userId, authorId, count);
            return count;
        } catch (NumberFormatException e) {
            log.error("解析未读计数时出错，用户ID={}，作者ID={}，错误: {}", userId, authorId, e.getMessage());
            return 0;
        } catch (Exception e) {
            log.error("获取未读计数时发生未知错误，用户ID={}，作者ID={}，错误: {}", 
                    userId, authorId, e.getMessage());
            return 0;
        }
    }
    
    @Override
    public void updateUnreadStatusAfterPublish(Long blogId, Long authorId, List<Long> followerIds) {
        log.info("发布博客后更新粉丝未读状态：blogId={}，authorId={}，粉丝数量={}", blogId, authorId, followerIds.size());
        
        if (followerIds.isEmpty()) {
            return;
        }
        
        try {
            // 1. 将博客添加到作者的博客集合
            String authorKey = AUTHOR_BLOGS_KEY + authorId;
            stringRedisTemplate.opsForSet().add(authorKey, blogId.toString());
            stringRedisTemplate.expire(authorKey, BLOG_INFO_TTL, TimeUnit.DAYS);
            
            // 2. 对每个粉丝更新未读状态
            for (Long followerId : followerIds) {
                // 2.1 添加到未读集合
                String unreadKey = USER_UNREAD_BLOGS_KEY + followerId + ":" + authorId;
                stringRedisTemplate.opsForSet().add(unreadKey, blogId.toString());
                
                // 2.2 更新未读计数
                String countKey = USER_UNREAD_COUNT_KEY + followerId;
                // 获取当前未读数量
                Object countObj = stringRedisTemplate.opsForHash().get(countKey, authorId.toString());
                String countStr = countObj == null ? "0" : countObj.toString();
                int count = 0;
                
                try {
                    if (!countStr.isEmpty()) {
                        count = Integer.parseInt(countStr);
                    }
                    
                    // 增加未读计数
                    count++;
                    stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), String.valueOf(count));
                    log.debug("更新粉丝[{}]对作者[{}]的未读计数: {} -> {}", followerId, authorId, 
                            countStr.isEmpty() ? 0 : Integer.parseInt(countStr), count);
                } catch (NumberFormatException e) {
                    log.error("解析未读计数时出错，粉丝ID={}，作者ID={}，值={}，错误: {}", 
                            followerId, authorId, countStr, e.getMessage());
                    // 重置计数为1（当前这篇博客）
                    stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), "1");
                }
                
                // 2.3 设置过期时间
                stringRedisTemplate.expire(unreadKey, USER_UNREAD_TTL, TimeUnit.DAYS);
                stringRedisTemplate.expire(countKey, USER_UNREAD_TTL, TimeUnit.DAYS);
            }
            
            log.info("更新粉丝未读状态完成：blogId={}", blogId);
        } catch (Exception e) {
            log.error("更新粉丝未读状态失败：blogId={}，error={}", blogId, e.getMessage(), e);
        }
    }
} 