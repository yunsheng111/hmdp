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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.FEED_KEY;

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

    @Resource
    private IUserService userService;

    @Resource
    private IFollowService followService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
        // 4.查询笔记作者的所有粉丝 select * from follow where follow_user_id = ?
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        // 5.推送笔记id给所有粉丝
        for (Follow follow : follows) {
            // 5.1获取粉丝id
            Long userId = follow.getUserId();
            // 5.2推送
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
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
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
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

        for (Blog blog : blogs) {
            // 5.1.查询blog有关的用户
            queryBlogUser(blog);
            // 5.2.查询blog是否被点赞
            isBlogLike(blog);
        }

        // 6.封装并返回
        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);

        return Result.success(r);
    }
}
