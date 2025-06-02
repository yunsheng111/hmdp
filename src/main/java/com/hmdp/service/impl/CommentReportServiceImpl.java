package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.CommentReportDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.CommentReport;
import com.hmdp.entity.ShopComment;
import com.hmdp.entity.User;
import com.hmdp.exception.ReportException;
import com.hmdp.mapper.CommentReportMapper;
import com.hmdp.mapper.ShopCommentMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.ICommentReportService;
import com.hmdp.service.IShopCommentService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论举报服务实现类
 */
@Service
public class CommentReportServiceImpl extends ServiceImpl<CommentReportMapper, CommentReport> implements ICommentReportService {

    @Resource
    private ShopCommentMapper shopCommentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private IShopCommentService shopCommentService;

    @Override
    @Transactional
    public Result createReport(CommentReportDTO reportDTO) {
        // 1. 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new ReportException("用户未登录");
        }

        // 2. 参数校验
        if (reportDTO.getCommentId() == null) {
            throw new ReportException("评论ID不能为空");
        }
        if (StrUtil.isBlank(reportDTO.getReason())) {
            throw new ReportException("举报理由不能为空");
        }

        // 3. 验证评论是否存在
        ShopComment comment = shopCommentMapper.selectById(reportDTO.getCommentId());
        if (comment == null) {
            throw new ReportException("评论不存在");
        }

        // 4. 验证用户是否已经举报过该评论
        LambdaQueryWrapper<CommentReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentReport::getCommentId, reportDTO.getCommentId())
                .eq(CommentReport::getReporterId, user.getId())
                .eq(CommentReport::getStatus, SystemConstants.REPORT_STATUS_PENDING);
        if (count(queryWrapper) > 0) {
            throw new ReportException("您已经举报过该评论，请等待处理");
        }

        // 5. 创建举报记录
        CommentReport report = new CommentReport();
        report.setCommentId(reportDTO.getCommentId());
        report.setReporterId(user.getId());
        report.setReason(reportDTO.getReason());
        report.setStatus(SystemConstants.REPORT_STATUS_PENDING);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        // 6. 保存举报记录
        save(report);

        // 7. 返回举报信息
        return Result.success();
    }

    @Override
    public Result queryPendingReports(Integer current) {
        // 1. 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new ReportException("用户未登录");
        }

        // TODO: 验证用户是否为管理员
        // 这里需要根据项目的权限管理机制来验证
        // 暂时使用一个假的验证方法，实际项目中需要替换为真实的权限验证逻辑
        boolean isAdmin = isAdmin(user.getId());
        if (!isAdmin) {
            throw new ReportException("无管理员权限");
        }

        // 2. 设置默认值
        current = current == null ? 1 : current;

        // 3. 创建分页对象
        Page<CommentReport> page = new Page<>(current, SystemConstants.COMMENT_PAGE_SIZE);

        // 4. 构建查询条件
        LambdaQueryWrapper<CommentReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentReport::getStatus, SystemConstants.REPORT_STATUS_PENDING);
        queryWrapper.orderByDesc(CommentReport::getCreateTime);

        // 5. 执行分页查询
        page = page(page, queryWrapper);

        // 6. 转换为DTO
        return convertToDTO(page);
    }

    @Override
    @Transactional
    public Result resolveReport(Long reportId, boolean approveReport) {
        // 1. 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new ReportException("用户未登录");
        }

        // TODO: 验证用户是否为管理员
        // 这里需要根据项目的权限管理机制来验证
        // 暂时使用一个假的验证方法，实际项目中需要替换为真实的权限验证逻辑
        boolean isAdmin = isAdmin(user.getId());
        if (!isAdmin) {
            throw new ReportException("无管理员权限");
        }

        // 2. 参数校验
        if (reportId == null) {
            throw new ReportException("举报ID不能为空");
        }

        // 3. 查询举报记录
        CommentReport report = getById(reportId);
        if (report == null) {
            throw new ReportException("举报记录不存在");
        }

        // 4. 验证举报记录状态
        if (report.getStatus() != SystemConstants.REPORT_STATUS_PENDING) {
            throw new ReportException("该举报已处理");
        }

        // 5. 处理举报
        if (approveReport) {
            // 删除评论
            Result result = shopCommentService.deleteCommentByAdmin(report.getCommentId());
            if (!result.getSuccess()) {
                throw new ReportException("删除评论失败: " + result.getErrorMsg());
            }
        }

        // 6. 更新举报记录
        report.setStatus(SystemConstants.REPORT_STATUS_RESOLVED);
        report.setUpdateTime(LocalDateTime.now());
        updateById(report);

        return Result.success();
    }

    /**
     * 将CommentReport分页结果转换为CommentReportDTO分页结果
     */
    private Result convertToDTO(Page<CommentReport> page) {
        // 1. 获取评论ID列表和用户ID列表
        List<Long> commentIds = page.getRecords().stream()
                .map(CommentReport::getCommentId)
                .collect(Collectors.toList());
        
        List<Long> userIds = page.getRecords().stream()
                .map(CommentReport::getReporterId)
                .collect(Collectors.toList());
        
        // 2. 批量查询评论和用户信息
        Map<Long, ShopComment> commentMap = new HashMap<>();
        if (!commentIds.isEmpty()) {
            List<ShopComment> comments = shopCommentMapper.selectBatchIds(commentIds);
            commentMap = comments.stream().collect(Collectors.toMap(ShopComment::getId, comment -> comment));
        }
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        }
        
        // 3. 转换为DTO
        List<CommentReportDTO> reportDTOs = new ArrayList<>();
        for (CommentReport report : page.getRecords()) {
            CommentReportDTO dto = new CommentReportDTO();
            BeanUtil.copyProperties(report, dto);
            
            // 设置举报者信息
            User reporter = userMap.get(report.getReporterId());
            if (reporter != null) {
                dto.setReporterName(reporter.getNickName());
            }
            
            // 设置评论信息
            ShopComment comment = commentMap.get(report.getCommentId());
            if (comment != null) {
                CommentReportDTO.CommentInfoDTO commentInfo = new CommentReportDTO.CommentInfoDTO();
                commentInfo.setId(comment.getId());
                commentInfo.setContent(comment.getContent());
                commentInfo.setRating(comment.getRating());
                commentInfo.setShopId(comment.getShopId());
                commentInfo.setUserId(comment.getUserId());
                
                // 查询评论用户信息
                User commentUser = userMapper.selectById(comment.getUserId());
                if (commentUser != null) {
                    commentInfo.setUserNickname(commentUser.getNickName());
                }
                
                dto.setComment(commentInfo);
            }
            
            reportDTOs.add(dto);
        }
        
        // 4. 构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", reportDTOs);
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return Result.success(result);
    }

    /**
     * 验证用户是否为管理员
     * 注意：这是一个临时方法，实际项目中需要替换为真实的权限验证逻辑
     */
    private boolean isAdmin(Long userId) {
        // TODO: 实现真实的管理员验证逻辑
        // 这里应该调用权限服务或查询用户角色表验证用户是否为管理员
        
        // 暂时返回true，模拟验证通过
        return true;
    }
} 