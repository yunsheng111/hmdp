package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

    // 查询放大因子，用于查询未读博客时放大查询范围
    private static final int AMPLIFICATION_FACTOR = 3;

    @Resource
    private IUserService userService;

    @Resource
    private IFollowService followService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    // 添加Lua脚本对象
    private static final DefaultRedisScript<Long> PUSH_TO_FAN_SCRIPT;
    private static final DefaultRedisScript<Long> MARK_READ_SCRIPT;
    private static final DefaultRedisScript<Long> HANDLE_DELETED_UNREAD_SCRIPT;
    
    // 静态初始化Lua脚本
    static {
        // 初始化推送博客脚本
        PUSH_TO_FAN_SCRIPT = new DefaultRedisScript<>();
        PUSH_TO_FAN_SCRIPT.setLocation(new ClassPathResource("lua/push_to_fan.lua"));
        PUSH_TO_FAN_SCRIPT.setResultType(Long.class);
        
        // 初始化标记已读脚本
        MARK_READ_SCRIPT = new DefaultRedisScript<>();
        MARK_READ_SCRIPT.setLocation(new ClassPathResource("lua/mark_read.lua"));
        MARK_READ_SCRIPT.setResultType(Long.class);
        
        // 初始化处理已删除未读博客脚本
        HANDLE_DELETED_UNREAD_SCRIPT = new DefaultRedisScript<>();
        HANDLE_DELETED_UNREAD_SCRIPT.setLocation(new ClassPathResource("lua/handle_deleted_unread.lua"));
        HANDLE_DELETED_UNREAD_SCRIPT.setResultType(Long.class);
    }

    /**
     * 根据博客ID查询博客详情
     *
     * @param id 博客ID
     * @return 查询到的博客对象，如果未找到则返回null
     */
    @Override
    public Result queryBlogById(Long id) {
        log.info("开始查询博客信息，id={}", id);
        //1. 查询博客
        Blog blog = getById(id);
        //2. 判断博客是否存在
        if (blog == null) {
            log.warn("博客不存在，id={}", id);
            return Result.fail("博客不存在");
        }
        
        // 确保images字段不为null，避免前端处理错误
        if (blog.getImages() == null) {
            blog.setImages("");
            log.info("博客images字段为null，已设置为空字符串，id={}", id);
        }
        
        //3.根据blog的userId查询作者信息
        queryBlogUser(blog);
        //4.查询当前用户是否点过赞
        isBlogLike(blog);
        
        log.info("查询博客成功，id={}，title={}, images={}", id, blog.getTitle(), blog.getImages());
        //4. 返回查询到的博客对象
        return Result.success(blog);
    }
    private void queryBlogUser(Blog blog) {
        //根据blog的userId查询作者信息
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setIcon(user.getIcon());
        blog.setName(user.getNickName());
    }

    /*
     * @description: 查询热门博客
     * @author: yate
     * @date: 2025/5/26 下午4:16
     * @param: [current]
     * @return: com.hmdp.common.Result
     **/
    @Override
    public Result queryHotBlog(Integer current) {
        //1.根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        //2.获取当前页数据
        List<Blog> records = page.getRecords();
        //3.查询用户
        records.forEach(blog ->{
            this.queryBlogUser(blog);
            this.isBlogLike(blog);
        });

        //4.返回查询结果
        return Result.success(records);

    }

    private void isBlogLike(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            //未登录用户，默认未点赞
            return;
        }
        //1.获取登录用户
        Long userId = user.getId();
        //2.判断当前用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score!= null);
    }

    @Override
    public Result likeBlog(Long id) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.判断当前用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            //3.如果未点赞，则进行点赞
            //3.1  数据库点赞数+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            //3.2  redis的zset集合中添加当前用户id    zadd key score member
            if (isSuccess) {
                stringRedisTemplate.opsForZSet()
                        .add(key, userId.toString(), System.currentTimeMillis());
            }
        }else {
            //4.如果已经点赞，则取消点赞
            //4.1  数据库点赞数-1
            boolean isFailure = update().setSql("liked = liked - 1").eq("id", id).update();
            //4.2  redis的set集合中删除当前用户id
            if (isFailure) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        //5.返回点赞结果
        return Result.success();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        //1.  查询top5的点赞用户 zrange key start end
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);

        if (top5 == null || top5.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        //2.  解析出用户id
        List<Long> userIds = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", userIds);


        //3. 根据用户id查询用户  SELECT * FROM user WHERE id IN (5 ,1) ORDER BY FIELD(id, 5,1)
        List<UserDTO> userDTOS = userService.query()
                .in("id", userIds)
                .last("ORDER BY FIELD(id, " + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        //4. 返回用户列表
        return Result.success(userDTOS);
    }

    /**
     * 保存博客
     *
     * @param blog 要保存的博客对象
     * @return 保存结果
     */
    @Override
    public Result saveBlog(Blog blog) {
        // 1.获取登录用户
        UserDTO user = UserHolder.getUser();
        // 2.设置博客的用户ID为登录用户的ID
        blog.setUserId(user.getId());
        // 3.保存探店博文
        save(blog);
        log.info("博客保存成功，id={}, userId={}", blog.getId(), user.getId());
        
        // 4.将博客作者ID存入Redis，用于后续获取作者ID
        String authorMapKey = BLOG_AUTHOR_MAP_KEY_PREFIX + blog.getId();
        stringRedisTemplate.opsForValue().set(authorMapKey, user.getId().toString());
        log.info("博客作者映射已保存，blogId={}，authorId={}", blog.getId(), user.getId());
        
        // 5.查询笔记作者的所有粉丝 select * from follow where follow_user_id = ?
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        log.info("博客作者粉丝数量: {}", follows.size());
        
        // 6.推送笔记id给所有粉丝，并更新未读计数
        long currentTime = System.currentTimeMillis();
        for (Follow follow : follows) {
            // 6.1获取粉丝id
            Long fanId = follow.getUserId();
            
            // 6.2构建Redis键
            String feedKey = FEED_KEY + fanId;
            String totalUnreadKey = TOTAL_UNREAD_COUNT_KEY_PREFIX + fanId;
            String authorUnreadKey = AUTHOR_UNREAD_COUNT_KEY_PREFIX + fanId + ":" + user.getId();
            
            // 6.3执行Lua脚本，推送博客并更新未读计数
            try {
                Long result = stringRedisTemplate.execute(
                    PUSH_TO_FAN_SCRIPT,
                    Arrays.asList(feedKey, totalUnreadKey, authorUnreadKey),
                    blog.getId().toString(), String.valueOf(currentTime)
                );
                log.info("推送博客到粉丝收件箱成功，粉丝id={}，博客id={}，当前未读数={}", fanId, blog.getId(), result);
            } catch (Exception e) {
                log.error("推送博客到粉丝收件箱失败，粉丝id={}，博客id={}，错误：{}", fanId, blog.getId(), e.getMessage());
            }
        }
        
        // 返回博客的ID
        return Result.success(blog.getId());
    }

    /**
     * 查询收件箱中博客的列表
     *
     * @param max 查询的最大时间戳，用于分页
     * @param offset 偏移量，用于分页
     * @return 查询结果
     */
    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        // 1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 2.查询收件箱 ZREVRANGEBYSCORE key Max Min LIMIT offset count
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, SystemConstants.MAX_PAGE_SIZE);
        // 3.非空判断
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.success();
        }
        // 4.解析数据：blogId、minTime（时间戳）、offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0; // 2
        int os = 1; // 2
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) { // 5 4 4 2 2
            // 4.1.获取id
            ids.add(Long.valueOf(tuple.getValue()));
            // 4.2.获取分数(时间戳）
            long time = tuple.getScore().longValue();
            if(time == minTime){
                os++;
            }else{
                minTime = time;
                os = 1;
            }
        }
        os = minTime == max ? os : os + offset;
        // 5.根据id查询blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();

        // 6.查询用户的已读博客列表
        String readKey = BLOG_READ_KEY + userId;
        Set<String> readBlogIds = stringRedisTemplate.opsForZSet().range(readKey, 0, -1);

        // 7.处理每个博客的信息
        for (Blog blog : blogs) {
            // 7.1.查询blog有关的用户
            queryBlogUser(blog);
            // 7.2.查询blog是否被点赞
            isBlogLike(blog);
            
            // 7.3.检查博客是否已被删除
            boolean isDeleted = stringRedisTemplate.opsForSet().isMember(DELETED_BLOG_HINTS_KEY, blog.getId().toString());
            blog.setIsDeleted(isDeleted);
            
            // 7.4.设置博客的已读状态，默认为未读
            blog.setIsRead(false);
            
            // 7.5.如果博客ID在已读列表中，则设置为已读
            if (readBlogIds != null && readBlogIds.contains(blog.getId().toString())) {
                blog.setIsRead(true);
            }
            
            // 7.6.如果博客已删除且之前未读，则调用脚本处理未读计数
            if (isDeleted && !blog.getIsRead()) {
                try {
                    // 获取博客作者ID
                    String authorMapKey = BLOG_AUTHOR_MAP_KEY_PREFIX + blog.getId();
                    String authorIdStr = stringRedisTemplate.opsForValue().get(authorMapKey);
                    Long authorId;
                    
                    if (authorIdStr == null) {
                        // 如果Redis中没有作者ID，则使用博客中的userId
                        authorId = blog.getUserId();
                        // 顺便将作者ID存入Redis
                        stringRedisTemplate.opsForValue().set(authorMapKey, authorId.toString());
                    } else {
                        authorId = Long.valueOf(authorIdStr);
                    }
                    
                    // 构建Redis键
                    String totalUnreadKey = TOTAL_UNREAD_COUNT_KEY_PREFIX + userId;
                    String authorUnreadKey = AUTHOR_UNREAD_COUNT_KEY_PREFIX + userId + ":" + authorId;
                    
                    // 执行Lua脚本，处理已删除的未读博客
                    Long result = stringRedisTemplate.execute(
                        HANDLE_DELETED_UNREAD_SCRIPT,
                        Arrays.asList(readKey, totalUnreadKey, authorUnreadKey),
                        blog.getId().toString(), String.valueOf(System.currentTimeMillis())
                    );
                    
                    log.info("已处理已删除的未读博客，userId={}，blogId={}，authorId={}，当前未读数={}", userId, blog.getId(), authorId, result);
                } catch (Exception e) {
                    log.error("处理已删除的未读博客失败，userId={}，blogId={}，错误：{}", userId, blog.getId(), e.getMessage());
                }
            }
        }

        // 8.封装并返回
        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        
        return Result.success(r);
    }
    
   

    /**
     * 标记博客为已读
     *
     * @param id 博客id
     * @return 操作结果
     */
    @Override
    public Result markBlogAsRead(Long id) {
        // 1.获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        try {
            // 2.从Redis获取博客作者ID
            String authorMapKey = BLOG_AUTHOR_MAP_KEY_PREFIX + id;
            String authorIdStr = stringRedisTemplate.opsForValue().get(authorMapKey);
            Long authorId;
            
            // 如果Redis中没有作者ID，则从数据库查询
            if (authorIdStr == null) {
                Blog blog = getById(id);
                if (blog == null) {
                    return Result.fail("博客不存在");
                }
                authorId = blog.getUserId();
                // 顺便将作者ID存入Redis
                stringRedisTemplate.opsForValue().set(authorMapKey, authorId.toString());
                log.info("从数据库获取并缓存博客作者ID，blogId={}，authorId={}", id, authorId);
            } else {
                authorId = Long.valueOf(authorIdStr);
            }
            
            // 3.构建Redis键
            String readKey = BLOG_READ_KEY + user.getId();
            String totalUnreadKey = TOTAL_UNREAD_COUNT_KEY_PREFIX + user.getId();
            String authorUnreadKey = AUTHOR_UNREAD_COUNT_KEY_PREFIX + user.getId() + ":" + authorId;
            
            // 4.执行Lua脚本，标记博客为已读并更新未读计数
            Long result = stringRedisTemplate.execute(
                MARK_READ_SCRIPT,
                Arrays.asList(readKey, totalUnreadKey, authorUnreadKey),
                id.toString(), String.valueOf(System.currentTimeMillis())
            );
            
            log.info("博客已标记为已读，userId={}，blogId={}，authorId={}，当前未读数={}", user.getId(), id, authorId, result);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("标记博客为已读失败，userId={}，blogId={}，错误：{}", user.getId(), id, e.getMessage());
            return Result.fail("操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID和阅读状态查询该用户的博客列表
     *
     * @param userId 用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @param readStatus 阅读状态，可选值为 "ALL"(所有) 或 "UNREAD"(未读)
     * @return 查询结果，包含分页信息
     */
    @Override
    public Result queryUserBlogByReadStatus(Long userId, Integer current, Integer size, String readStatus) {
        log.info("开始查询用户博客，userId={}，current={}，size={}，readStatus={}", userId, current, size, readStatus);
        
        // 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("用户未登录");
        }
        
        // 查询当前登录用户的已读博客ID集合
        String readKey = BLOG_READ_KEY + currentUser.getId();
        Set<String> readBlogIds = stringRedisTemplate.opsForZSet().range(readKey, 0, -1);
        
        // 判断是否需要过滤未读博客
        boolean filterUnread = "UNREAD".equals(readStatus);
        
        // 如果需要过滤未读博客，查询更大批量的数据
        int querySize = filterUnread ? size * AMPLIFICATION_FACTOR : size;
        
        // 查询指定用户的博客列表
        Page<Blog> page = this.query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .page(new Page<>(current, querySize));
        
        // 获取查询结果
        List<Blog> records = page.getRecords();
        List<Blog> filteredRecords = records;
        
        // 如果需要过滤未读博客，在内存中过滤
        if (filterUnread && readBlogIds != null && !readBlogIds.isEmpty()) {
            filteredRecords = records.stream()
                    .filter(blog -> !readBlogIds.contains(blog.getId().toString()))
                    .collect(Collectors.toList());
            
            // 限制结果数量为请求的size
            if (filteredRecords.size() > size) {
                filteredRecords = filteredRecords.subList(0, size);
            }
            
            log.info("过滤后的博客数量: {}", filteredRecords.size());
        }
        
        // 设置博客的用户信息和已读状态
        for (Blog blog : filteredRecords) {
            // 设置博客用户信息
            queryBlogUser(blog);
            // 设置博客点赞状态
            isBlogLike(blog);
            // 设置博客已读状态
            blog.setIsRead(readBlogIds != null && readBlogIds.contains(blog.getId().toString()));
        }
        
        // 创建新的Page对象返回过滤后的结果
        Page<Blog> resultPage = new Page<>(current, size, page.getTotal());
        resultPage.setRecords(filteredRecords);
        
        log.info("查询用户博客完成，userId={}，结果数量={}", userId, filteredRecords.size());
        
        return Result.success(resultPage);
    }

    /**
     * 获取用户的未读计数
     * 
     * @return 包含总未读数和按作者未读数的结果
     */
    @Override
    public Result getUnreadCounts() {
        // 1.获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        try {
            // 2.获取用户的总未读数
            String totalUnreadKey = TOTAL_UNREAD_COUNT_KEY_PREFIX + user.getId();
            String totalUnreadStr = stringRedisTemplate.opsForValue().get(totalUnreadKey);
            int totalUnread = totalUnreadStr == null ? 0 : Integer.parseInt(totalUnreadStr);
            
            // 3.查询用户关注的所有作者
            List<Follow> follows = followService.query().eq("user_id", user.getId()).list();
            
            // 4.创建结果Map
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("total", totalUnread);
            
            // 5.获取每个作者的未读数
            Map<String, Integer> authorUnreads = new java.util.HashMap<>();
            for (Follow follow : follows) {
                Long authorId = follow.getFollowUserId();
                String authorUnreadKey = AUTHOR_UNREAD_COUNT_KEY_PREFIX + user.getId() + ":" + authorId;
                String authorUnreadStr = stringRedisTemplate.opsForValue().get(authorUnreadKey);
                int authorUnread = authorUnreadStr == null ? 0 : Integer.parseInt(authorUnreadStr);
                
                // 只添加有未读消息的作者
                if (authorUnread > 0) {
                    // 获取作者信息
                    User author = userService.getById(authorId);
                    if (author != null) {
                        // 使用作者名称作为键
                        authorUnreads.put(author.getNickName(), authorUnread);
                    } else {
                        // 如果作者不存在，使用ID作为键
                        authorUnreads.put(authorId.toString(), authorUnread);
                    }
                }
            }
            
            result.put("authors", authorUnreads);
            
            log.info("获取用户未读计数成功，userId={}，总未读数={}，作者未读数={}", user.getId(), totalUnread, authorUnreads);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户未读计数失败，userId={}，错误：{}", user.getId(), e.getMessage());
            return Result.fail("获取未读计数失败：" + e.getMessage());
        }
    }
}
