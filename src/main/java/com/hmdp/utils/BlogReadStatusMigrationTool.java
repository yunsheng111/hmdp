package com.hmdp.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.*;

/**
 * 博客阅读状态数据迁移工具
 * 
 * 该工具类在系统启动时自动执行一次，将旧的博客阅读状态数据从原有的Redis存储结构(blog:read:userId)
 * 迁移到新的Redis存储结构中，包括:
 * 1. 博客基本信息(blog:info:blogId)
 * 2. 用户已读博客集合(user:read:userId)
 * 3. 作者博客集合(author:blogs:authorId)
 * 4. 用户未读博客集合(user:unread:userId:authorId)
 * 5. 用户未读计数(user:unread:count:userId)
 * 
 * 迁移过程只会在系统启动后执行一次，不会重复执行，确保数据平滑过渡到新的存储结构。
 */
@Component
@Slf4j
public class BlogReadStatusMigrationTool {
    // 定义旧的博客阅读状态key前缀，根据项目文档，原有系统使用这个key
    private static final String BLOG_READ_KEY = "blog:read:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private IBlogService blogService;
    
    @Resource
    private IFollowService followService;
    
    /**
     * 系统启动后延迟10秒执行一次数据迁移
     * 该方法将旧的Redis数据结构迁移到新的结构中
     */
    @Scheduled(initialDelay = 10000, fixedRate = Long.MAX_VALUE)
    public void initializeRedisData() {
        log.info("开始迁移博客阅读状态数据到Redis...");
        
        try {
            // 1. 迁移所有博客信息到Redis
            migrateAllBlogInfo();
            
            // 2. 迁移作者博客集合
            migrateAuthorBlogs();
            
            // 3. 迁移已读状态记录
            migrateReadStatus();
            
            log.info("博客阅读状态数据迁移完成");
        } catch (Exception e) {
            log.error("数据迁移过程中发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 迁移所有博客信息到Redis
     */
    private void migrateAllBlogInfo() {
        log.info("开始迁移博客信息");
        int count = 0;
        
        // 分批次查询所有博客
        int pageSize = 100;
        int current = 1;
        
        while (true) {
            // 分页查询博客
            Page<Blog> pageParam = new Page<>(current, pageSize);
            Page<Blog> page = blogService.page(pageParam);
            List<Blog> blogs = page.getRecords();
            
            if (blogs.isEmpty()) {
                break;
            }
            
            // 批量迁移博客信息
            for (Blog blog : blogs) {
                String key = BLOG_INFO_KEY + blog.getId();
                Map<String, String> blogMap = new HashMap<>();
                blogMap.put("id", blog.getId().toString());
                blogMap.put("userId", blog.getUserId().toString());
                blogMap.put("title", blog.getTitle());
                blogMap.put("createTime", String.valueOf(blog.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                
                stringRedisTemplate.opsForHash().putAll(key, blogMap);
                stringRedisTemplate.expire(key, BLOG_INFO_TTL, TimeUnit.DAYS);
                
                count++;
            }
            
            current++;
            
            if (blogs.size() < pageSize) {
                break;
            }
        }
        
        log.info("博客信息迁移完成，共迁移{}条记录", count);
    }
    
    /**
     * 迁移作者博客集合到Redis
     */
    private void migrateAuthorBlogs() {
        log.info("开始迁移作者博客集合");
        int count = 0;
        
        // 查询所有博客的作者ID
        List<Long> authorIds = blogService.query()
                .select("DISTINCT user_id")
                .list()
                .stream()
                .map(Blog::getUserId)
                .collect(Collectors.toList());
        
        log.info("找到{}个博客作者", authorIds.size());
        
        for (Long authorId : authorIds) {
            // 查询该作者的所有博客
            List<Blog> authorBlogs = blogService.query().eq("user_id", authorId).list();
            
            if (!authorBlogs.isEmpty()) {
                String key = AUTHOR_BLOGS_KEY + authorId;
                // 转换为字符串数组
                String[] blogIds = authorBlogs.stream()
                        .map(blog -> blog.getId().toString())
                        .toArray(String[]::new);
                
                // 添加到作者博客集合
                stringRedisTemplate.opsForSet().add(key, blogIds);
                stringRedisTemplate.expire(key, BLOG_INFO_TTL, TimeUnit.DAYS);
                
                count += authorBlogs.size();
            }
        }
        
        log.info("作者博客集合迁移完成，共迁移{}条记录", count);
    }
    
    /**
     * 迁移已读记录到新的Redis结构
     */
    private void migrateReadStatus() {
        log.info("开始迁移已读记录");
        int count = 0;
        
        // 查询旧的已读记录键
        Set<String> readKeys = stringRedisTemplate.keys(BLOG_READ_KEY + "*");
        
        if (readKeys == null || readKeys.isEmpty()) {
            log.info("没有找到已读记录，跳过迁移");
            return;
        }
        
        log.info("找到{}个用户的已读记录", readKeys.size());
        
        for (String oldKey : readKeys) {
            try {
                // 解析用户ID
                String userIdStr = oldKey.substring(BLOG_READ_KEY.length());
                Long userId = Long.valueOf(userIdStr);
                
                // 获取该用户的所有已读博客ID
                Set<String> readBlogIds = stringRedisTemplate.opsForZSet().range(oldKey, 0, -1);
                
                if (readBlogIds == null || readBlogIds.isEmpty()) {
                    continue;
                }
                
                // 将已读记录添加到新的键中
                String newReadKey = USER_READ_BLOGS_KEY + userId;
                String[] blogIdArray = readBlogIds.toArray(new String[0]);
                stringRedisTemplate.opsForSet().add(newReadKey, blogIdArray);
                stringRedisTemplate.expire(newReadKey, USER_READ_TTL, TimeUnit.DAYS);
                
                // 更新未读集合和计数
                updateUnreadStatus(userId, readBlogIds);
                
                count += readBlogIds.size();
            } catch (Exception e) {
                log.error("迁移用户已读记录失败: {}", oldKey, e);
            }
        }
        
        log.info("已读记录迁移完成，共迁移{}条记录", count);
    }
    
    /**
     * 更新用户的未读状态
     *
     * @param userId 用户ID
     * @param readBlogIds 已读博客ID集合
     */
    private void updateUnreadStatus(Long userId, Set<String> readBlogIds) {
        // 查询用户关注的作者
        List<Follow> follows = followService.query().eq("user_id", userId).list();
        
        for (Follow follow : follows) {
            Long authorId = follow.getFollowUserId();
            
            // 查询作者的所有博客
            String authorKey = AUTHOR_BLOGS_KEY + authorId;
            Set<String> authorBlogIds = stringRedisTemplate.opsForSet().members(authorKey);
            
            // 如果Redis中没有，则从数据库查询
            if (authorBlogIds == null || authorBlogIds.isEmpty()) {
                List<Blog> authorBlogs = blogService.query().eq("user_id", authorId).list();
                
                if (authorBlogs.isEmpty()) {
                    continue;
                }
                
                // 添加到Redis
                authorBlogIds = authorBlogs.stream()
                        .map(blog -> blog.getId().toString())
                        .collect(Collectors.toSet());
                
                String[] blogIdArray = authorBlogIds.toArray(new String[0]);
                stringRedisTemplate.opsForSet().add(authorKey, blogIdArray);
                stringRedisTemplate.expire(authorKey, BLOG_INFO_TTL, TimeUnit.DAYS);
            }
            
            // 计算未读博客
            Set<String> unreadBlogIds = authorBlogIds.stream()
                    .filter(blogId -> !readBlogIds.contains(blogId))
                    .collect(Collectors.toSet());
            
            if (!unreadBlogIds.isEmpty()) {
                // 更新未读集合
                String unreadKey = USER_UNREAD_BLOGS_KEY + userId + ":" + authorId;
                String[] unreadArray = unreadBlogIds.toArray(new String[0]);
                stringRedisTemplate.opsForSet().add(unreadKey, unreadArray);
                stringRedisTemplate.expire(unreadKey, USER_UNREAD_TTL, TimeUnit.DAYS);
                
                // 更新未读计数
                String countKey = USER_UNREAD_COUNT_KEY + userId;
                stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), String.valueOf(unreadBlogIds.size()));
                stringRedisTemplate.expire(countKey, USER_UNREAD_TTL, TimeUnit.DAYS);
            }
        }
    }
} 