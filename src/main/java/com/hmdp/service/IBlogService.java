package com.hmdp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.Blog;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 根据id查询博客详情
     *
     * @param id 博客的id
     * @return 返回查询到的博客详情
     */
    Result queryBlogById(Long id);

    /*
     * @description: 查询热门博客
     * @author: yate
     * @date: 2025/5/26 下午4:15
     * @param: [current]
     * @return: com.hmdp.common.Result
     **/
    Result queryHotBlog(Integer current);

    /**
     * 根据博客ID点赞博客
     *
     * @param id 博客ID
     * @return 点赞结果
     */
    Result likeBlog(Long id);


    /*
     * @description: 博客点赞排行榜
     * @author: yate
     * @date: 2025/5/26 下午6:49
     * @param: [id]
     * @return: com.hmdp.common.Result
     **/
    Result queryBlogLikes(Long id);

    /*
     * @description: 添加博客
     * @author: yate
     * @date: 2025/5/27 19:02
     * @param: [blog]
     * @return: com.hmdp.common.Result
     **/
    Result saveBlog(Blog blog);

    /**
     *  查询收件箱中博客的列表
     *
     * @param max 查询的最大时间戳
     * @param offset 偏移量，用于分页
     * @return 查询结果
     */
    Result queryBlogOfFollow(Long max, Integer offset);
    
    /**
     * 标记博客为已读
     *
     * @param id 博客id
     * @return 操作结果
     */
    Result markBlogAsRead(Long id);
    
    /**
     * 根据用户ID和阅读状态查询该用户的博客列表
     *
     * @param userId 用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @param readStatus 阅读状态，可选值为 "ALL"(所有) 或 "UNREAD"(未读)
     * @return 查询结果，包含分页信息
     */
    Result queryUserBlogByReadStatus(Long userId, Integer current, Integer size, String readStatus);

    /**
     * 获取用户的未读计数
     * 
     * @return 包含总未读数和按作者未读数的结果
     */
    Result getUnreadCounts();
}
