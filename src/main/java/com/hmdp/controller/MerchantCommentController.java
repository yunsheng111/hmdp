package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.MerchantCommentQueryDTO;
import com.hmdp.dto.MerchantCommentReplyDTO;
import com.hmdp.dto.MerchantCommentReportDTO;
import com.hmdp.service.IMerchantCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 商家评论管理控制器
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
@Slf4j
@RestController
@RequestMapping("/merchant/comment")
@Api(tags = "商家端-评论管理接口")
public class MerchantCommentController {

    @Resource
    private IMerchantCommentService merchantCommentService;

    /**
     * 获取评论列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取评论列表", notes = "获取当前商家店铺的评论列表，支持分页、筛选、排序")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", dataType = "int", paramType = "query", example = "1"),
            @ApiImplicitParam(name = "size", value = "每页大小", dataType = "int", paramType = "query", example = "10"),
            @ApiImplicitParam(name = "rating", value = "按评分筛选(1-5)", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "按状态筛选(0-正常,1-用户隐藏,2-管理员隐藏)", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "hasReply", value = "是否已回复筛选", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "sortBy", value = "排序字段(time,rating)", dataType = "string", paramType = "query", example = "time"),
            @ApiImplicitParam(name = "order", value = "排序方向(asc,desc)", dataType = "string", paramType = "query", example = "desc")
    })
    public Result getCommentList(MerchantCommentQueryDTO queryDTO) {
        log.info("商家获取评论列表，查询参数: {}", queryDTO);
        return merchantCommentService.getCommentList(queryDTO);
    }

    /**
     * 商家回复评论
     */
    @PostMapping("/{commentId}/reply")
    @ApiOperation(value = "商家回复评论", notes = "商家对指定评论进行回复")
    @ApiImplicitParam(name = "commentId", value = "评论ID", required = true, dataType = "long", paramType = "path")
    public Result replyComment(@PathVariable Long commentId, 
                              @Valid @RequestBody MerchantCommentReplyDTO replyDTO) {
        log.info("商家回复评论，评论ID: {}, 回复内容: {}", commentId, replyDTO.getContent());
        return merchantCommentService.replyComment(commentId, replyDTO);
    }

    /**
     * 修改商家回复
     */
    @PutMapping("/{commentId}/reply")
    @ApiOperation(value = "修改商家回复", notes = "修改已有的商家回复")
    @ApiImplicitParam(name = "commentId", value = "评论ID", required = true, dataType = "long", paramType = "path")
    public Result updateReply(@PathVariable Long commentId, 
                             @Valid @RequestBody MerchantCommentReplyDTO replyDTO) {
        log.info("修改商家回复，评论ID: {}, 新回复内容: {}", commentId, replyDTO.getContent());
        return merchantCommentService.updateReply(commentId, replyDTO);
    }

    /**
     * 删除商家回复
     */
    @DeleteMapping("/{commentId}/reply")
    @ApiOperation(value = "删除商家回复", notes = "删除商家回复")
    @ApiImplicitParam(name = "commentId", value = "评论ID", required = true, dataType = "long", paramType = "path")
    public Result deleteReply(@PathVariable Long commentId) {
        log.info("删除商家回复，评论ID: {}", commentId);
        return merchantCommentService.deleteReply(commentId);
    }

    /**
     * 获取评论统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取评论统计数据", notes = "获取当前商家店铺的评论统计信息")
    public Result getStatistics() {
        log.info("获取评论统计数据");
        return merchantCommentService.getStatistics();
    }

    /**
     * 举报不当评论
     */
    @PostMapping("/{commentId}/report")
    @ApiOperation(value = "举报不当评论", notes = "商家举报不当评论")
    @ApiImplicitParam(name = "commentId", value = "评论ID", required = true, dataType = "long", paramType = "path")
    public Result reportComment(@PathVariable Long commentId, 
                               @Valid @RequestBody MerchantCommentReportDTO reportDTO) {
        log.info("举报不当评论，评论ID: {}, 举报原因: {}", commentId, reportDTO.getReason());
        return merchantCommentService.reportComment(commentId, reportDTO);
    }
}
