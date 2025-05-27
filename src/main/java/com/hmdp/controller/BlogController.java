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

    //  发布博客
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    // 点赞博客
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    //获取登录用户的博客列表
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

    // 根据用户ID查询该用户的博客列表，分页展示
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

    // 根据点赞数量降序查询热门博客列表，分页展示
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /*
     * @description: 根据博客id查询博客详情
     * @author: yate
     * @date: 2025/5/26 下午2:57
     * @param: [id]
     * @return: com.hmdp.common.Result
     **/
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id){
        return blogService.queryBlogById(id);
    }

    /*
     * @description: 博客点赞排行榜
     * @author: yate
     * @date: 2025/5/26 下午6:48
     * @param: [id]
     * @return: com.hmdp.common.Result
     **/
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id){
        return blogService.queryBlogLikes(id);
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

    /**
     * 收件箱博客的分页查询
     *
     * @param max 最后一条博客的时间戳，用于分页查询
     * @param offset 偏移量，默认为0
     * @return 查询结果
     */
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset){
        return blogService.queryBlogOfFollow(max,offset);
    }

}
