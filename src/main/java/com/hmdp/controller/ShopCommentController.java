package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.ShopCommentDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.exception.CommentException;
import com.hmdp.service.IShopCommentService;
import com.hmdp.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 商店评论控制器
 * </p>
 *
 * @author yate
 * @since 2024-07-29
 */
@Api(tags = "商铺评论接口")
@RestController
@RequestMapping("/shop-comment")
public class ShopCommentController {

    @Resource
    private IShopCommentService shopCommentService;

    /**
     * 创建评论
     *
     * @param commentDTO 评论信息
     * @return 结果
     */
    @ApiOperation(value = "创建评论", notes = "用户为商店创建评论，必须关联到有效订单")
    @PostMapping
    public Result createComment(@RequestBody ShopCommentDTO commentDTO) {
        // 验证用户是否登录
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new CommentException("用户未登录");
        }
        return shopCommentService.createComment(commentDTO);
    }

    /**
     * 查询商店评论列表
     *
     * @param shopId 商店ID
     * @param current 当前页码
     * @param sortBy 排序方式：time（时间）、rating（评分）、hotness（热度）
     * @param order 排序顺序：asc（升序）、desc（降序）
     * @return 结果
     */
    @ApiOperation(value = "查询商店评论列表", notes = "获取指定商店的评论列表，支持分页和排序")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "shopId", value = "商店ID", required = true, dataType = "Long", paramType = "path"),
        @ApiImplicitParam(name = "current", value = "当前页码，默认1", required = false, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "sortBy", value = "排序方式：time（时间）、rating（评分）、hotness（热度），默认time", required = false, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "order", value = "排序顺序：asc（升序）、desc（降序），默认desc", required = false, dataType = "String", paramType = "query")
    })
    @GetMapping("/shop/{shopId}")
    public Result queryShopComments(
            @PathVariable("shopId") Long shopId,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order
    ) {
        if (shopId == null) {
            throw new CommentException("商店ID不能为空");
        }
        return shopCommentService.queryShopComments(shopId, current, sortBy, order);
    }

    /**
     * 用户删除自己的评论
     *
     * @param commentId 评论ID
     * @return 结果
     */
    @ApiOperation(value = "用户删除自己的评论", notes = "用户删除自己的评论（软删除）")
    @ApiImplicitParam(name = "id", value = "评论ID", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable("id") Long commentId) {
        // 验证用户是否登录
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new CommentException("用户未登录");
        }
        if (commentId == null) {
            throw new CommentException("评论ID不能为空");
        }
        return shopCommentService.deleteCommentByUser(commentId);
    }

    /**
     * 管理员删除评论
     *
     * @param commentId 评论ID
     * @return 结果
     */
    @ApiOperation(value = "管理员删除评论", notes = "管理员删除评论（软删除），需要管理员权限")
    @ApiImplicitParam(name = "id", value = "评论ID", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/admin/{id}")
    public Result deleteCommentByAdmin(@PathVariable("id") Long commentId) {
        // 验证用户是否登录
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new CommentException("用户未登录");
        }
        if (commentId == null) {
            throw new CommentException("评论ID不能为空");
        }
        return shopCommentService.deleteCommentByAdmin(commentId);
    }

    /**
     * 计算商店平均评分
     *
     * @param shopId 商店ID
     * @return 结果
     */
    @ApiOperation(value = "计算商店平均评分", notes = "计算并返回商店的平均评分")
    @ApiImplicitParam(name = "shopId", value = "商店ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/score/{shopId}")
    public Result calculateShopAverageRating(@PathVariable("shopId") Long shopId) {
        if (shopId == null) {
            throw new CommentException("商店ID不能为空");
        }
        return shopCommentService.calculateShopAverageRating(shopId);
    }
}