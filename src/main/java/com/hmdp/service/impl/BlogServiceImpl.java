package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
    @Resource
    private IUserService userService;

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
        //1. 查询博客
        Blog blog = getById(id);
        //2. 判断博客是否存在
        if (blog == null) {
            return Result.fail("博客不存在");
        }
        //3.根据blog的userId查询作者信息
        queryBlogUser(blog);
        //4.查询当前用户是否点过赞
        isBlogLike(blog);
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
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.判断当前用户是否已经点赞
        String key = "blog:like:" + blog.getId();
        Boolean isMenber = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        blog.setIsLike(BooleanUtil.isTrue(isMenber));
    }

    @Override
    public Result likeBlog(Long id) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.判断当前用户是否已经点赞
        String key = "blog:like:" + id;
        Boolean isMenber = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtil.isFalse(isMenber)) {
            //3.如果未点赞，则进行点赞
            //3.1  数据库点赞数+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            //3.2  redis的set集合中添加当前用户id
            if (isSuccess) {
                stringRedisTemplate.opsForSet().add("blog:like:" + id, userId.toString());
            }
        }else {
            //4.如果已经点赞，则取消点赞
            //4.1  数据库点赞数-1
            boolean isFailure = update().setSql("liked = liked - 1").eq("id", id).update();
            //4.2  redis的set集合中删除当前用户id
            if (isFailure) {
                stringRedisTemplate.opsForSet().remove("blog:like:" + id, userId.toString());
            }
        }
        //5.返回点赞结果
        return Result.success();
    }
}
