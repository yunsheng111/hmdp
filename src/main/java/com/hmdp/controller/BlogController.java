package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.DELETED_BLOG_HINTS_KEY;

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
    private static final Logger log = LoggerFactory.getLogger(BlogController.class);

    @Resource
    private IBlogService blogService; 
    @Resource
    private IUserService userService; 
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    // 根据用户ID查询该用户的博客列表，分页展示（保留原方法保持兼容性）
    @GetMapping("/of/user")
    public Result queryUserBlog(
            @RequestParam("id") Long userId, // 用户ID作为查询参数
            @RequestParam(value = "current", defaultValue = "1") Integer current, // 当前页码，默认为1
            @RequestParam(value = "size", defaultValue = "10") Integer size) { // 每页大小，默认为10
        // 根据用户ID查询该用户的博客列表，分页展示
        // 使用传入的size参数，但不超过MAX_PAGE_SIZE
        int pageSize = size; // 直接使用前端请求的size
        Page<Blog> page = blogService.query()
                .eq("user_id", userId).page(new Page<>(current, pageSize));
        // 获取当前页的数据
        List<Blog> records = page.getRecords();
        // 批量查询用户信息，避免N+1查询问题
        if (!records.isEmpty()) {
            // 收集所有需要查询的用户ID
            Set<Long> userIds = records.stream()
                    .map(Blog::getUserId)
                    .collect(Collectors.toSet());

            // 批量查询用户信息
            List<User> users = userService.listByIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));

            // 设置博客的用户信息
            records.forEach(blog -> {
                User user = userMap.get(blog.getUserId());
                if (user != null) {
                    blog.setName(user.getNickName());
                    blog.setIcon(user.getIcon());
                }
            });
        }
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
        
        try {
            // 将博客ID添加到已删除博客提示集合中
            stringRedisTemplate.opsForSet().add(DELETED_BLOG_HINTS_KEY, id.toString());
            log.info("博客已添加到已删除提示集合，blogId={}", id);
            
            // 删除博客
            boolean success = blogService.removeById(id);
            
            if (success) {
                log.info("博客删除成功，blogId={}", id);
                return Result.success();
            } else {
                log.error("博客删除失败，blogId={}", id);
                return Result.fail("删除失败");
            }
        } catch (Exception e) {
            log.error("博客删除过程中发生错误，blogId={}，错误：{}", id, e.getMessage());
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 收件箱博客的分页查询（未读博客）
     *
     * @param max 最后一条博客的时间戳，用于分页查询
     * @param offset 偏移量，默认为0
     * @param authorId 可选的博主ID，用于筛选特定博主的博客
     * @return 查询结果
     */
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam(value = "lastId", required = false) Long max,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "authorId", required = false) Long authorId){
        return blogService.queryBlogOfFollow(max, offset, authorId);
    }
    /**
     * 标记博客为已读
     * 
     * @param id 博客id
     * @return 操作结果
     */
    @PostMapping("/read/{id}")
    public Result markBlogAsRead(@PathVariable("id") Long id) {
        return blogService.markBlogAsRead(id);
    }

    /**
     * 获取用户的未读计数
     *
     * @return 包含总未读数和按作者未读数的结果
     */
    @GetMapping("/unread/counts")
    public Result getUnreadCounts() {
        return blogService.getUnreadCounts();
    }

    /**
     * 获取消息页面数据
     *
     * @return 消息页面初始化数据，包含总未读数、关注作者列表、最近未读博客
     */
    @GetMapping("/message/page")
    public Result getMessagePageData() {
        return blogService.getMessagePageData();
    }
}
