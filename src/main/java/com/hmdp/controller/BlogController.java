package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService; 
    @Resource
    private IUserService userService; 

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 设置博客的用户ID为登录用户的ID
        blog.setUserId(user.getId());
        // 保存探店博文
        blogService.save(blog);
        // 返回博客的ID
        return Result.success(blog.getId());
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        // 修改点赞数量，针对指定ID的博客点赞数加1
        blogService.update()
                .setSql("liked = liked + 1").eq("id", id).update();
        // 返回成功结果
        return Result.success();
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户ID查询该用户的博客列表，分页展示
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页的数据
        List<Blog> records = page.getRecords();
        // 返回查询到的博客列表
        return Result.success(records);
    }
    
    @GetMapping("/of/user")
    public Result queryUserBlog(
            @RequestParam("id") Long userId, // 用户ID作为查询参数
            @RequestParam(value = "current", defaultValue = "1") Integer current) { // 当前页码，默认为1
        // 根据用户ID查询该用户的博客列表，分页展示
        Page<Blog> page = blogService.query()
                .eq("user_id", userId).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页的数据
        List<Blog> records = page.getRecords();
        // 遍历每个博客，查询对应的用户信息，并将用户昵称和头像设置到博客对象中
        records.forEach(blog ->{
            Long blogUserId = blog.getUserId();
            User user = userService.getById(blogUserId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
        // 返回包含用户信息的博客列表
        return Result.success(page);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据点赞数量降序查询热门博客列表，分页展示
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页的数据
        List<Blog> records = page.getRecords();
        // 遍历每个热门博客，查询对应的用户信息，并将用户昵称和头像设置到博客对象中
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
        // 返回热门博客列表
        return Result.success(records);
    }
    /*
     * 根据id查询博客详情
     */
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id){
        // 查询博客
        Blog blog = blogService.getById(id);
        // 返回博客
        return Result.success(blog);
    }

    /**
     * 删除博客
     * @param id 博客id
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteBlog(@PathVariable("id") Long id) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 查询博客
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.fail("博客不存在");
        }
        
        // 判断是否是自己的博客
        if (!blog.getUserId().equals(user.getId())) {
            return Result.fail("无权限删除他人博客");
        }
        
        // 删除博客
        boolean success = blogService.removeById(id);
        
        return success ? Result.success() : Result.fail("删除失败");
    }
}
