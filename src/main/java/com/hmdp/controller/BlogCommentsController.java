package com.hmdp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.BlogComments;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogCommentsService;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  博客评论前端控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {

    private static final Logger log = LoggerFactory.getLogger(BlogCommentsController.class);

    @Resource
    private IBlogCommentsService blogCommentsService;

    @Resource
    private IUserService userService;
    
    @Resource
    private IBlogService blogService;

    /**
     * 获取博客的评论列表
     *
     * @param blogId 博客ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    @GetMapping("/blog/{blogId}")
    public Result getBlogComments(
            @PathVariable("blogId") Long blogId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        log.info("获取博客评论，博客ID：{}，页码：{}，每页大小：{}", blogId, page, size);
        
        try {
            // 获取当前登录用户
            UserDTO currentUser = UserHolder.getUser();
            Long currentUserId = currentUser != null ? currentUser.getId() : null;
            
            // 查询一级评论（parentId = 0）
            LambdaQueryWrapper<BlogComments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BlogComments::getBlogId, blogId)
                    .eq(BlogComments::getParentId, 0L) // 只查询一级评论
                    .eq(BlogComments::getStatus, 0) // 只查询正常状态的评论
                    .orderByDesc(BlogComments::getCreateTime); // 按创建时间降序排序
            
            // 创建分页对象
            Page<BlogComments> commentsPage = new Page<>(page, size);
            
            // 执行分页查询
            Page<BlogComments> result = blogCommentsService.page(commentsPage, queryWrapper);
            
            // 获取评论列表
            List<BlogComments> commentsList = result.getRecords();
            
            // 处理评论列表，添加用户信息和回复
            List<Map<String, Object>> enhancedComments = commentsList.stream().map(comment -> {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("id", comment.getId());
                commentMap.put("content", comment.getContent());
                commentMap.put("liked", comment.getLiked());
                commentMap.put("createTime", comment.getCreateTime());
                
                // 添加用户信息
                User user = userService.getById(comment.getUserId());
                if (user != null) {
                    commentMap.put("userId", user.getId());
                    commentMap.put("userNickName", user.getNickName());
                    commentMap.put("userIcon", user.getIcon());
                }
                
                // 查询该评论的回复
                LambdaQueryWrapper<BlogComments> repliesQuery = new LambdaQueryWrapper<>();
                repliesQuery.eq(BlogComments::getParentId, comment.getId())
                        .eq(BlogComments::getStatus, 0)
                        .orderByAsc(BlogComments::getCreateTime);
                List<BlogComments> replies = blogCommentsService.list(repliesQuery);
                
                // 处理回复列表，添加用户信息
                List<Map<String, Object>> enhancedReplies = replies.stream().map(reply -> {
                    Map<String, Object> replyMap = new HashMap<>();
                    replyMap.put("id", reply.getId());
                    replyMap.put("content", reply.getContent());
                    replyMap.put("createTime", reply.getCreateTime());
                    
                    // 添加回复者信息
                    User replyUser = userService.getById(reply.getUserId());
                    if (replyUser != null) {
                        replyMap.put("userId", replyUser.getId());
                        replyMap.put("userNickName", replyUser.getNickName());
                    }
                    
                    // 如果是回复其他评论，添加被回复者信息
                    if (reply.getAnswerId() > 0) {
                        User answerUser = userService.getById(reply.getAnswerId());
                        if (answerUser != null) {
                            replyMap.put("answerId", answerUser.getId());
                            replyMap.put("answerName", answerUser.getNickName());
                        }
                    }
                    
                    return replyMap;
                }).collect(Collectors.toList());
                
                commentMap.put("replies", enhancedReplies);
                
                // 检查当前用户是否点赞过该评论
                commentMap.put("isLiked", false); // TODO: 实现点赞检查逻辑
                
                return commentMap;
            }).collect(Collectors.toList());
            
            // 构建返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("records", enhancedComments);
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            data.put("size", result.getSize());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取博客评论失败，博客ID：{}", blogId, e);
            return Result.fail("获取评论失败：" + e.getMessage());
        }
    }

    /**
     * 添加博客评论
     *
     * @param blogComments 评论信息
     * @return 添加结果
     */
    @PostMapping
    public Result addComment(@RequestBody BlogComments blogComments) {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        try {
            // 设置评论用户ID
            blogComments.setUserId(user.getId());
            // 设置评论状态为正常
            blogComments.setStatus(0);
            // 设置点赞数为0
            blogComments.setLiked(0);
            // 设置父评论ID，如果未指定则默认为0（一级评论）
            if (blogComments.getParentId() == null) {
                blogComments.setParentId(0L);
            }
            // 如果是一级评论，确保answerId也有默认值
            if (blogComments.getParentId() == 0L && blogComments.getAnswerId() == null) {
                blogComments.setAnswerId(0L);
            }
            // 设置创建时间
            blogComments.setCreateTime(LocalDateTime.now());
            // 设置更新时间
            blogComments.setUpdateTime(LocalDateTime.now());
            
            // 保存评论
            boolean success = blogCommentsService.save(blogComments);
            
            if (success) {
                // 更新博客评论数+1
                Long blogId = blogComments.getBlogId();
                boolean updateSuccess = blogService.update()
                        .setSql("comments = comments + 1")
                        .eq("id", blogId)
                        .update();
                
                if (!updateSuccess) {
                    log.warn("更新博客评论数失败，博客ID：{}", blogId);
                }
                
                return Result.success(blogComments);
            } else {
                return Result.fail("评论发布失败");
            }
        } catch (Exception e) {
            log.error("添加评论失败", e);
            return Result.fail("评论发布失败：" + e.getMessage());
        }
    }

    /**
     * 点赞评论
     *
     * @param id 评论ID
     * @return 点赞结果
     */
    @PutMapping("/like/{id}")
    public Result likeComment(@PathVariable("id") Long id) {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        try {
            // 查询评论
            BlogComments comment = blogCommentsService.getById(id);
            if (comment == null) {
                return Result.fail("评论不存在");
            }
            
            // TODO: 实现点赞逻辑，需要使用Redis记录用户点赞状态
            // 临时实现：直接增加点赞数
            comment.setLiked(comment.getLiked() + 1);
            comment.setUpdateTime(LocalDateTime.now());
            
            boolean success = blogCommentsService.updateById(comment);
            
            if (success) {
                return Result.success();
            } else {
                return Result.fail("点赞失败");
            }
        } catch (Exception e) {
            log.error("点赞评论失败，评论ID：{}", id, e);
            return Result.fail("点赞失败：" + e.getMessage());
        }
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable("id") Long id) {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        try {
            // 查询评论
            BlogComments comment = blogCommentsService.getById(id);
            if (comment == null) {
                return Result.fail("评论不存在");
            }
            
            // 判断是否是自己的评论
            if (!comment.getUserId().equals(user.getId())) {
                return Result.fail("无权限删除他人评论");
            }
            
            // 获取博客ID用于后续更新评论数
            Long blogId = comment.getBlogId();
            
            // 删除评论（软删除，修改状态）
            comment.setStatus(1); // 设置为已删除状态
            comment.setUpdateTime(LocalDateTime.now());
            
            boolean success = blogCommentsService.updateById(comment);
            
            if (success) {
                // 更新博客评论数-1
                // 先查询当前博客评论数，确保不会减为负数
                Blog blog = blogService.getById(blogId);
                if (blog != null && blog.getComments() != null && blog.getComments() > 0) {
                    boolean updateSuccess = blogService.update()
                            .setSql("comments = comments - 1")
                            .eq("id", blogId)
                            .update();
                    
                    if (!updateSuccess) {
                        log.warn("更新博客评论数失败，博客ID：{}", blogId);
                    }
                } else {
                    log.warn("博客评论数已为0或博客不存在，跳过减法操作，博客ID：{}", blogId);
                }
                
                return Result.success();
            } else {
                return Result.fail("删除失败");
            }
        } catch (Exception e) {
            log.error("删除评论失败，评论ID：{}", id, e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }
}
