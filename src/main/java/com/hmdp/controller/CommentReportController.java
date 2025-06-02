package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.CommentReportDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.exception.ReportException;
import com.hmdp.service.ICommentReportService;
import com.hmdp.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 评论举报控制器
 * </p>
 *
 * @author yate
 * @since 2024-07-29
 */
@Api(tags = "评论举报接口")
@RestController
@RequestMapping("/comment-report")
public class CommentReportController {

    @Resource
    private ICommentReportService commentReportService;

    /**
     * 举报评论
     *
     * @param reportDTO 举报信息
     * @return 结果
     */
    @ApiOperation(value = "举报评论", notes = "商家举报不当评论")
    @PostMapping
    public Result reportComment(@RequestBody CommentReportDTO reportDTO) {
        // 验证用户是否登录
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new ReportException("用户未登录");
        }
        return commentReportService.createReport(reportDTO);
    }

    /**
     * 查询待处理的举报列表
     * 管理员使用
     *
     * @param current 当前页码
     * @return 结果
     */
    @ApiOperation(value = "查询待处理的举报列表", notes = "管理员查询待处理的举报列表，需要管理员权限")
    @ApiImplicitParam(name = "current", value = "当前页码，默认1", required = false, dataType = "Integer", paramType = "query")
    @GetMapping("/pending")
    public Result queryPendingReports(@RequestParam(value = "current", required = false) Integer current) {
        // 验证用户是否登录
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new ReportException("用户未登录");
        }
        return commentReportService.queryPendingReports(current);
    }

    /**
     * 处理举报
     * 管理员使用
     *
     * @param reportId 举报ID
     * @param approve 是否批准举报（true=批准并隐藏评论，false=拒绝举报）
     * @return 结果
     */
    @ApiOperation(value = "处理举报", notes = "管理员处理举报（批准或拒绝），需要管理员权限")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "举报ID", required = true, dataType = "Long", paramType = "path"),
        @ApiImplicitParam(name = "approve", value = "是否批准举报（true=批准并隐藏评论，false=拒绝举报）", required = true, dataType = "Boolean", paramType = "query")
    })
    @PostMapping("/resolve/{id}")
    public Result resolveReport(
            @PathVariable("id") Long reportId,
            @RequestParam("approve") Boolean approve
    ) {
        // 验证用户是否登录
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new ReportException("用户未登录");
        }
        if (reportId == null) {
            throw new ReportException("举报ID不能为空");
        }
        if (approve == null) {
            throw new ReportException("处理决定不能为空");
        }
        return commentReportService.resolveReport(reportId, approve);
    }
} 