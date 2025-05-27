package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.entity.Blog;

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
}
