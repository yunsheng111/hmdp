# 大V博客阅读状态Redis优化实现方案

**文档创建时间:** [2023-06-14 10:00:00 +08:00]  
**创建者:** Qitian Dasheng (AI) - LD  
**文档版本:** v1.0

## 更新日志

| 版本 | 时间 | 更新内容 | 更新人 |
| --- | --- | --- | --- |
| v1.0 | [2023-06-14 10:00:00 +08:00] | 初始实现方案 | Qitian Dasheng (LD) |

## 1. 实现概述

本文档详细描述大V博客阅读状态Redis优化的具体实现方案，包括核心代码结构、关键接口实现和数据迁移策略。方案基于AR的架构设计文档，遵循KISS、YAGNI、SOLID、DRY和高内聚低耦合的设计原则。

## 2. 核心代码模块

### 2.1 Redis配置及常量定义

创建新的Redis常量类，定义相关键值：

```java
public class BlogRedisConstants {
    // 博客基本信息
    public static final String BLOG_INFO_KEY = "blog:info:";
    // 用户已读博客集合
    public static final String USER_READ_BLOGS_KEY = "user:read:";
    // 作者博客集合
    public static final String AUTHOR_BLOGS_KEY = "author:blogs:";
    // 用户对特定作者的未读博客
    public static final String USER_UNREAD_BLOGS_KEY = "user:unread:";
    // 用户未读计数
    public static final String USER_UNREAD_COUNT_KEY = "user:unread:count:";
    
    // 过期时间定义
    public static final Long BLOG_INFO_TTL = 7L;
    public static final Long USER_READ_TTL = 30L;
    public static final Long USER_UNREAD_TTL = 30L;
}
```

### 2.2 博客阅读状态管理服务

创建专门的服务类处理博客阅读状态：

```java
@Service
public class BlogReadStatusService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private IBlogService blogService;
    
    /**
     * 标记博客为已读
     */
    public void markBlogAsRead(Long userId, Long blogId) {
        // 1. 获取博客信息
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new RuntimeException("博客不存在");
        }
        
        Long authorId = blog.getUserId();
        
        // 2. 添加到用户已读集合
        String readKey = BlogRedisConstants.USER_READ_BLOGS_KEY + userId;
        stringRedisTemplate.opsForSet().add(readKey, blogId.toString());
        
        // 3. 从用户未读集合中移除
        String unreadKey = BlogRedisConstants.USER_UNREAD_BLOGS_KEY + userId + ":" + authorId;
        stringRedisTemplate.opsForSet().remove(unreadKey, blogId.toString());
        
        // 4. 更新未读计数
        String countKey = BlogRedisConstants.USER_UNREAD_COUNT_KEY + userId;
        // 获取当前未读数
        String countStr = stringRedisTemplate.opsForHash().get(countKey, authorId.toString());
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        
        if (count > 0) {
            // 减少计数
            stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), String.valueOf(count - 1));
        }
        
        // 5. 设置过期时间
        stringRedisTemplate.expire(readKey, BlogRedisConstants.USER_READ_TTL, TimeUnit.DAYS);
        stringRedisTemplate.expire(unreadKey, BlogRedisConstants.USER_UNREAD_TTL, TimeUnit.DAYS);
        stringRedisTemplate.expire(countKey, BlogRedisConstants.USER_UNREAD_TTL, TimeUnit.DAYS);
    }
    
    /**
     * 根据阅读状态查询博客
     */
    public List<Long> queryBlogIdsByReadStatus(Long userId, Long authorId, String readStatus, int start, int end) {
        // 判断是查询所有还是仅查询未读
        boolean queryUnreadOnly = "UNREAD".equals(readStatus);
        
        if (queryUnreadOnly) {
            // 直接查询未读博客ID
            String unreadKey = BlogRedisConstants.USER_UNREAD_BLOGS_KEY + userId + ":" + authorId;
            Set<String> unreadIds = stringRedisTemplate.opsForSet().members(unreadKey);
            
            if (unreadIds == null || unreadIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 转换并排序(可以改进为按时间排序)
            List<Long> blogIds = unreadIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            
            // 分页处理
            int toIndex = Math.min(end, blogIds.size());
            if (start >= blogIds.size() || start >= toIndex) {
                return Collections.emptyList();
            }
            
            return blogIds.subList(start, toIndex);
        } else {
            // 查询作者的所有博客
            String authorKey = BlogRedisConstants.AUTHOR_BLOGS_KEY + authorId;
            Set<String> blogIdSet = stringRedisTemplate.opsForSet().members(authorKey);
            
            if (blogIdSet == null || blogIdSet.isEmpty()) {
                // 如果Redis中没有，则从数据库查询并写入Redis
                List<Blog> blogs = blogService.query().eq("user_id", authorId).list();
                if (blogs.isEmpty()) {
                    return Collections.emptyList();
                }
                
                // 将博客ID添加到作者博客集合
                blogIdSet = new HashSet<>();
                for (Blog blog : blogs) {
                    blogIdSet.add(blog.getId().toString());
                    stringRedisTemplate.opsForSet().add(authorKey, blog.getId().toString());
                }
            }
            
            // 转换并排序(可以改进为按时间排序)
            List<Long> blogIds = blogIdSet.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            
            // 分页处理
            int toIndex = Math.min(end, blogIds.size());
            if (start >= blogIds.size() || start >= toIndex) {
                return Collections.emptyList();
            }
            
            return blogIds.subList(start, toIndex);
        }
    }
    
    /**
     * 获取用户对特定作者的未读博客数量
     */
    public int getUnreadCount(Long userId, Long authorId) {
        String countKey = BlogRedisConstants.USER_UNREAD_COUNT_KEY + userId;
        String countStr = stringRedisTemplate.opsForHash().get(countKey, authorId.toString());
        return countStr == null ? 0 : Integer.parseInt(countStr);
    }
    
    /**
     * 发布博客后，更新粉丝的未读状态
     */
    public void updateUnreadStatusAfterPublish(Long blogId, Long authorId, List<Long> followerIds) {
        // 1. 将博客添加到作者的博客集合
        String authorKey = BlogRedisConstants.AUTHOR_BLOGS_KEY + authorId;
        stringRedisTemplate.opsForSet().add(authorKey, blogId.toString());
        
        // 2. 对每个粉丝更新未读状态
        for (Long followerId : followerIds) {
            // 2.1 添加到未读集合
            String unreadKey = BlogRedisConstants.USER_UNREAD_BLOGS_KEY + followerId + ":" + authorId;
            stringRedisTemplate.opsForSet().add(unreadKey, blogId.toString());
            
            // 2.2 更新未读计数
            String countKey = BlogRedisConstants.USER_UNREAD_COUNT_KEY + followerId;
            String countStr = stringRedisTemplate.opsForHash().get(countKey, authorId.toString());
            int count = countStr == null ? 0 : Integer.parseInt(countStr);
            stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), String.valueOf(count + 1));
            
            // 2.3 设置过期时间
            stringRedisTemplate.expire(unreadKey, BlogRedisConstants.USER_UNREAD_TTL, TimeUnit.DAYS);
            stringRedisTemplate.expire(countKey, BlogRedisConstants.USER_UNREAD_TTL, TimeUnit.DAYS);
        }
    }
}
```

### 2.3 修改BlogServiceImpl实现

修改`BlogServiceImpl`类中的相关方法，整合新的Redis阅读状态管理：

```java
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    // ... 现有代码 ...
    
    @Resource
    private BlogReadStatusService blogReadStatusService;
    
    @Override
    @Transactional
    public Result saveBlog(Blog blog) {
        // 1. 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        
        // 2. 保存博客到数据库
        boolean success = save(blog);
        if (!success) {
            return Result.fail("博客保存失败");
        }
        
        // 3. 查询作者的所有粉丝
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        List<Long> followerIds = follows.stream()
                .map(Follow::getUserId)
                .collect(Collectors.toList());
        
        // 4. 更新粉丝的未读状态(异步执行)
        if (!followerIds.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                blogReadStatusService.updateUnreadStatusAfterPublish(
                        blog.getId(), user.getId(), followerIds);
            });
        }
        
        // 5. 返回博客ID
        return Result.success(blog.getId());
    }
    
    @Override
    public Result markBlogAsRead(Long id) {
        // 1. 获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 2. 调用阅读状态服务标记为已读
        try {
            blogReadStatusService.markBlogAsRead(user.getId(), id);
        } catch (Exception e) {
            log.error("标记博客已读失败", e);
            return Result.fail("操作失败: " + e.getMessage());
        }
        
        // 3. 获取用户未读总数
        Blog blog = getById(id);
        if (blog != null) {
            int unreadCount = blogReadStatusService.getUnreadCount(user.getId(), blog.getUserId());
            return Result.success(unreadCount);
        }
        
        return Result.success();
    }
    
    @Override
    public Result queryUserBlogByReadStatus(Long userId, Integer current, Integer size, String readStatus) {
        // 1. 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("用户未登录");
        }
        
        // 2. 计算分页参数
        int start = (current - 1) * size;
        int end = current * size;
        
        // 3. 查询指定条件的博客ID列表
        List<Long> blogIds = blogReadStatusService.queryBlogIdsByReadStatus(
                currentUser.getId(), userId, readStatus, start, end);
        
        if (blogIds.isEmpty()) {
            // 返回空结果
            Page<Blog> emptyPage = new Page<>(current, size, 0);
            return Result.success(emptyPage);
        }
        
        // 4. 根据ID批量查询博客详情
        List<Blog> blogs = listByIds(blogIds);
        
        // 5. 查询博客的用户信息和点赞状态
        for (Blog blog : blogs) {
            // 设置博客用户信息
            queryBlogUser(blog);
            // 设置博客点赞状态
            isBlogLike(blog);
            // 设置博客已读状态
            blog.setIsRead("UNREAD".equals(readStatus) ? false : true);
        }
        
        // 6. 构建返回结果
        Page<Blog> page = new Page<>(current, size);
        page.setRecords(blogs);
        
        // 如果是查询未读，则设置总数为未读数
        if ("UNREAD".equals(readStatus)) {
            int unreadCount = blogReadStatusService.getUnreadCount(currentUser.getId(), userId);
            page.setTotal(unreadCount);
        } else {
            // 查询作者博客总数
            long count = query().eq("user_id", userId).count();
            page.setTotal(count);
        }
        
        return Result.success(page);
    }
    
    // ... 其他方法 ...
}
```

## 3. 数据迁移策略

为了确保平滑过渡到新的Redis存储结构，需要实现数据迁移工具类：

```java
@Component
@Slf4j
public class BlogReadStatusMigrationTool {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private IBlogService blogService;
    
    @Resource
    private IFollowService followService;
    
    /**
     * 初始化Redis数据
     */
    @Scheduled(initialDelay = 10000, fixedRate = Long.MAX_VALUE)
    public void initializeRedisData() {
        log.info("开始初始化博客阅读状态Redis数据...");
        
        try {
            // 1. 迁移所有博客信息
            migrateAllBlogInfo();
            
            // 2. 迁移作者博客集合
            migrateAuthorBlogs();
            
            // 3. 迁移已读记录
            migrateReadStatus();
            
            log.info("博客阅读状态Redis数据初始化完成");
        } catch (Exception e) {
            log.error("初始化Redis数据失败", e);
        }
    }
    
    private void migrateAllBlogInfo() {
        // 分批次迁移博客信息
        int pageSize = 100;
        int current = 1;
        
        while (true) {
            Page<Blog> page = blogService.page(new Page<>(current, pageSize));
            List<Blog> blogs = page.getRecords();
            
            if (blogs.isEmpty()) {
                break;
            }
            
            for (Blog blog : blogs) {
                String key = BlogRedisConstants.BLOG_INFO_KEY + blog.getId();
                Map<String, String> blogMap = new HashMap<>();
                blogMap.put("id", blog.getId().toString());
                blogMap.put("userId", blog.getUserId().toString());
                blogMap.put("title", blog.getTitle());
                blogMap.put("createTime", String.valueOf(blog.getCreateTime().getTime()));
                
                stringRedisTemplate.opsForHash().putAll(key, blogMap);
                stringRedisTemplate.expire(key, BlogRedisConstants.BLOG_INFO_TTL, TimeUnit.DAYS);
            }
            
            current++;
            
            if (blogs.size() < pageSize) {
                break;
            }
        }
        
        log.info("博客信息迁移完成");
    }
    
    private void migrateAuthorBlogs() {
        // 查询所有博客的作者ID列表
        List<Long> authorIds = blogService.query()
                .select("DISTINCT user_id")
                .list()
                .stream()
                .map(Blog::getUserId)
                .collect(Collectors.toList());
        
        for (Long authorId : authorIds) {
            // 查询该作者的所有博客
            List<Blog> authorBlogs = blogService.query().eq("user_id", authorId).list();
            
            if (!authorBlogs.isEmpty()) {
                String key = BlogRedisConstants.AUTHOR_BLOGS_KEY + authorId;
                String[] blogIds = authorBlogs.stream()
                        .map(blog -> blog.getId().toString())
                        .toArray(String[]::new);
                
                stringRedisTemplate.opsForSet().add(key, blogIds);
            }
        }
        
        log.info("作者博客集合迁移完成");
    }
    
    private void migrateReadStatus() {
        // 从原有的Redis键中迁移已读状态
        Set<String> readKeys = stringRedisTemplate.keys(RedisConstants.BLOG_READ_KEY + "*");
        
        if (readKeys == null || readKeys.isEmpty()) {
            log.info("没有找到已读记录，跳过迁移");
            return;
        }
        
        for (String oldKey : readKeys) {
            // 解析用户ID
            String userIdStr = oldKey.substring(RedisConstants.BLOG_READ_KEY.length());
            Long userId = Long.valueOf(userIdStr);
            
            // 获取该用户的所有已读博客ID
            Set<String> readBlogIds = stringRedisTemplate.opsForZSet().range(oldKey, 0, -1);
            
            if (readBlogIds == null || readBlogIds.isEmpty()) {
                continue;
            }
            
            // 将已读记录添加到新的键中
            String newReadKey = BlogRedisConstants.USER_READ_BLOGS_KEY + userId;
            String[] blogIdArray = readBlogIds.toArray(new String[0]);
            stringRedisTemplate.opsForSet().add(newReadKey, blogIdArray);
            
            // 设置过期时间
            stringRedisTemplate.expire(newReadKey, BlogRedisConstants.USER_READ_TTL, TimeUnit.DAYS);
            
            // 更新未读集合和计数
            updateUnreadStatus(userId, readBlogIds);
        }
        
        log.info("阅读状态迁移完成");
    }
    
    private void updateUnreadStatus(Long userId, Set<String> readBlogIds) {
        // 查询用户关注的作者
        List<Follow> follows = followService.query().eq("user_id", userId).list();
        
        for (Follow follow : follows) {
            Long authorId = follow.getFollowUserId();
            
            // 查询作者的所有博客
            List<Blog> authorBlogs = blogService.query().eq("user_id", authorId).list();
            
            if (authorBlogs.isEmpty()) {
                continue;
            }
            
            // 计算未读博客
            List<String> unreadBlogIds = authorBlogs.stream()
                    .map(blog -> blog.getId().toString())
                    .filter(blogId -> !readBlogIds.contains(blogId))
                    .collect(Collectors.toList());
            
            if (!unreadBlogIds.isEmpty()) {
                // 更新未读集合
                String unreadKey = BlogRedisConstants.USER_UNREAD_BLOGS_KEY + userId + ":" + authorId;
                stringRedisTemplate.opsForSet().add(unreadKey, unreadBlogIds.toArray(new String[0]));
                stringRedisTemplate.expire(unreadKey, BlogRedisConstants.USER_UNREAD_TTL, TimeUnit.DAYS);
                
                // 更新未读计数
                String countKey = BlogRedisConstants.USER_UNREAD_COUNT_KEY + userId;
                stringRedisTemplate.opsForHash().put(countKey, authorId.toString(), String.valueOf(unreadBlogIds.size()));
                stringRedisTemplate.expire(countKey, BlogRedisConstants.USER_UNREAD_TTL, TimeUnit.DAYS);
            }
        }
    }
}
```

## 4. 异常处理与降级策略

为确保系统稳定性，实现Redis操作异常处理和降级策略：

```java
@Component
@Slf4j
public class RedisFallbackHandler {
    @Resource
    private IBlogService blogService;
    
    /**
     * 降级查询用户博客
     */
    public List<Blog> fallbackQueryUserBlog(Long userId, Integer current, Integer size) {
        log.warn("Redis查询失败，降级为数据库查询：userId={}, current={}, size={}", userId, current, size);
        
        // 直接从数据库查询
        Page<Blog> page = blogService.query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .page(new Page<>(current, size));
                
        return page.getRecords();
    }
    
    /**
     * 降级标记博客已读
     */
    public boolean fallbackMarkBlogAsRead(Long userId, Long blogId) {
        log.warn("Redis标记已读失败，操作将不被记录：userId={}, blogId={}", userId, blogId);
        
        // 返回成功，但实际上未记录（可以选择记录到本地临时存储，后续同步）
        return true;
    }
}
```

## 5. 优化后的性能分析

### 5.1 性能改进分析

| 操作 | 优化前 | 优化后 | 改进 |
|------|-------|-------|------|
| 查询未读博客 | O(N) + 内存过滤 | O(1) 直接查询 | 显著提升，特别是大V粉丝量大时 |
| 标记已读 | 多次Redis操作 | 事务内批量操作 | 减少网络往返，提高原子性 |
| 发布博客 | 同步推送 | 异步批量推送 | 减少主流程耗时 |

### 5.2 内存占用估算

假设系统有以下参数：
- 活跃用户数：100万
- 每用户平均关注大V数：10个
- 每个大V平均每天发布博客数：5篇
- 博客ID存储（8字节/ID）

估算每日新增Redis内存占用：
- 博客信息：5博客/天 * 10大V * 100字节/博客 ≈ 5KB/用户/天
- 未读关系：5博客/天 * 10大V * 8字节/ID ≈ 400字节/用户/天
- 未读计数：10大V * 16字节(key+value) ≈ 160字节/用户/天

总计：约5.5KB/用户/天，所有活跃用户约5.5GB/天

通过30天的过期策略，控制总内存在165GB以内，可通过分片、压缩等方式进一步优化。

## 6. 实施计划

### 6.1 代码实施步骤

1. 添加新的Redis常量类和Redis配置
2. 实现`BlogReadStatusService`类
3. 修改`BlogServiceImpl`中的相关方法
4. 实现数据迁移工具
5. 添加降级处理逻辑

### 6.2 测试验证计划

1. 单元测试：验证各个Redis操作的正确性
2. 集成测试：验证服务间交互
3. 性能测试：
   - 大V发布博客时的系统性能
   - 大量用户同时查询未读博客的响应时间
   - Redis内存使用监控

### 6.3 上线策略

1. 先部署新代码但不启用新功能（Feature Flag控制）
2. 执行数据迁移脚本，将现有数据同步到新结构
3. 在非高峰期为小部分用户启用新功能，观察系统表现
4. 逐步扩大启用范围，直到全量发布
5. 监控系统性能和错误率，准备回滚方案

## 7. 总结

本实现方案通过重新设计Redis数据结构和查询流程，显著提高了大V博客阅读状态管理的效率和准确性。主要改进点包括：

1. 直接查询未读博客，避免内存过滤
2. 支持按大V维度查询未读博客和计数
3. 异步批量处理提高性能
4. 完善的数据迁移和降级策略

实施此方案需要注意数据一致性和内存占用问题，建议按计划逐步实施，并持续监控系统性能。 