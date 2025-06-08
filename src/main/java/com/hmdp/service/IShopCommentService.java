package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.dto.ShopCommentDTO;
import com.hmdp.entity.ShopComment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商店评论服务接口
 * </p>
 *
 * @author yate
 * @since 2024-07-29
 */
public interface IShopCommentService extends IService<ShopComment> {

    /**
     * 创建评论
     * 处理orderId验证，从UserHolder获取用户信息
     *
     * @param commentDTO 评论信息
     * @return 结果
     */
    Result createComment(ShopCommentDTO commentDTO);

    /**
     * 查询商店评论列表
     * 处理分页，按时间、评分、热度排序
     *
     * @param shopId 商店ID
     * @param current 当前页码
     * @param sortBy 排序字段，可选值："time"、"rating"、"hotness"
     * @param order 排序方向，可选值："asc"、"desc"
     * @return 结果
     */
    Result queryShopComments(Long shopId, Integer current, String sortBy, String order);

    /**
     * 用户删除自己的评论（软删除）
     * 验证评论所有权
     *
     * @param commentId 评论ID
     * @return 结果
     */
    Result deleteCommentByUser(Long commentId);

    /**
     * 管理员删除评论（软删除）
     *
     * @param commentId 评论ID
     * @return 结果
     */
    Result deleteCommentByAdmin(Long commentId);

    /**
     * 计算商店平均评分
     *
     * @param shopId 商店ID
     * @return 结果
     */
    Result calculateShopAverageRating(Long shopId);

   
}