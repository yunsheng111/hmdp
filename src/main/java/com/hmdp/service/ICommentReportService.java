package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.dto.CommentReportDTO;
import com.hmdp.entity.CommentReport;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评论举报服务接口
 * </p>
 *
 * @author AI (Qitian Dasheng)
 * @since 2024-07-29
 */
public interface ICommentReportService extends IService<CommentReport> {

    /**
     * 创建举报
     * 从UserHolder获取商家ID
     *
     * @param reportDTO 举报信息
     * @return 结果
     */
    Result createReport(CommentReportDTO reportDTO);

    /**
     * 查询待处理的举报列表
     * 管理员使用
     *
     * @param current 当前页码
     * @return 结果
     */
    Result queryPendingReports(Integer current);

    /**
     * 处理举报
     * 管理员使用
     *
     * @param reportId 举报ID
     * @param approveReport 是否批准举报（true=批准并隐藏评论，false=拒绝举报）
     * @return 结果
     */
    Result resolveReport(Long reportId, boolean approveReport);
} 