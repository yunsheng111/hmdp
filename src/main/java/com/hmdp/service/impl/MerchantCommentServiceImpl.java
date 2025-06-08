package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.Result;
import com.hmdp.dto.*;
import com.hmdp.entity.CommentReport;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopComment;
import com.hmdp.entity.User;
import com.hmdp.exception.CommentException;
import com.hmdp.mapper.CommentReportMapper;
import com.hmdp.mapper.ShopCommentMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IMerchantCommentService;
import com.hmdp.service.IShopService;
import com.hmdp.utils.MerchantHolder;
import lombok.extern.slf4j.Slf4j;
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
 * <p>
 * 商家评论管理服务实现
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
@Slf4j
@Service
public class MerchantCommentServiceImpl implements IMerchantCommentService {

    @Resource
    private ShopCommentMapper shopCommentMapper;

    @Resource
    private CommentReportMapper commentReportMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private IShopService shopService;

    /**
     * 获取评论列表
     */
    @Override
    public Result getCommentList(MerchantCommentQueryDTO queryDTO) {
        try {
            // 验证并设置默认值
            queryDTO.validate();

            // 获取当前商家信息
            MerchantDTO merchant = MerchantHolder.getMerchant();
            if (merchant == null) {
                throw new CommentException("商家未登录");
            }

            // 构建查询条件
            QueryWrapper<ShopComment> queryWrapper = new QueryWrapper<>();

            // 根据shopId参数决定查询范围
            if (queryDTO.getShopId() != null) {
                // 查询特定店铺的评论，需要验证店铺归属
                validateShopOwnership(merchant.getId(), queryDTO.getShopId());
                queryWrapper.eq("shop_id", queryDTO.getShopId());
                log.info("查询特定店铺评论，商家ID: {}, 店铺ID: {}", merchant.getId(), queryDTO.getShopId());
            } else {
                // 查询商家所有店铺的评论
                List<Long> shopIds = getMerchantShopIds(merchant.getId());
                if (shopIds.isEmpty()) {
                    log.info("商家 {} 暂无店铺，返回空评论列表", merchant.getId());
                    return Result.success(createEmptyPageResult());
                }
                queryWrapper.in("shop_id", shopIds);
                log.info("查询商家所有店铺评论，商家ID: {}, 店铺数量: {}", merchant.getId(), shopIds.size());
            }

            // 添加筛选条件
            if (queryDTO.getRating() != null) {
                queryWrapper.eq("rating", queryDTO.getRating());
            }
            if (queryDTO.getStatus() != null) {
                queryWrapper.eq("status", queryDTO.getStatus());
            }
            if (queryDTO.getHasReply() != null) {
                if (queryDTO.getHasReply()) {
                    queryWrapper.isNotNull("reply");
                } else {
                    queryWrapper.isNull("reply");
                }
            }

            // 添加排序条件
            if ("rating".equals(queryDTO.getSortBy())) {
                if ("asc".equals(queryDTO.getOrder())) {
                    queryWrapper.orderByAsc("rating");
                } else {
                    queryWrapper.orderByDesc("rating");
                }
            } else {
                // 默认按时间排序
                if ("asc".equals(queryDTO.getOrder())) {
                    queryWrapper.orderByAsc("create_time");
                } else {
                    queryWrapper.orderByDesc("create_time");
                }
            }

            // 分页查询
            Page<ShopComment> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            Page<ShopComment> result = shopCommentMapper.selectPage(page, queryWrapper);

            // 转换为DTO并填充用户信息
            List<MerchantCommentListDTO> commentList = result.getRecords().stream()
                    .map(this::convertToListDTO)
                    .collect(Collectors.toList());

            // 构建分页结果
            Map<String, Object> data = new HashMap<>();
            data.put("records", commentList);
            data.put("total", result.getTotal());
            data.put("current", result.getCurrent());
            data.put("size", result.getSize());

            return Result.success(data);

        } catch (CommentException e) {
            log.error("获取评论列表失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取评论列表异常", e);
            return Result.fail("获取评论列表失败");
        }
    }

    /**
     * 商家回复评论
     */
    @Override
    @Transactional
    public Result replyComment(Long commentId, MerchantCommentReplyDTO replyDTO) {
        try {
            // 验证评论权限
            ShopComment comment = validateCommentPermission(commentId);

            // 检查是否已有回复
            if (comment.getReply() != null && !comment.getReply().trim().isEmpty()) {
                throw new CommentException("该评论已有回复，请使用修改接口");
            }

            // 更新回复内容
            comment.setReply(replyDTO.getContent());
            comment.setUpdateTime(LocalDateTime.now());

            int updated = shopCommentMapper.updateById(comment);
            if (updated == 0) {
                throw new CommentException("回复失败，请重试");
            }

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("commentId", commentId);
            data.put("reply", replyDTO.getContent());
            data.put("replyTime", comment.getUpdateTime());

            log.info("商家回复评论成功，评论ID: {}", commentId);
            return Result.success(data);

        } catch (CommentException e) {
            log.error("商家回复评论失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("商家回复评论异常", e);
            return Result.fail("回复失败，请重试");
        }
    }

    /**
     * 修改商家回复
     */
    @Override
    @Transactional
    public Result updateReply(Long commentId, MerchantCommentReplyDTO replyDTO) {
        try {
            // 验证评论权限
            ShopComment comment = validateCommentPermission(commentId);

            // 检查是否有回复
            if (comment.getReply() == null || comment.getReply().trim().isEmpty()) {
                throw new CommentException("该评论尚未回复，请使用回复接口");
            }

            // 更新回复内容
            comment.setReply(replyDTO.getContent());
            comment.setUpdateTime(LocalDateTime.now());

            int updated = shopCommentMapper.updateById(comment);
            if (updated == 0) {
                throw new CommentException("修改回复失败，请重试");
            }

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("commentId", commentId);
            data.put("reply", replyDTO.getContent());
            data.put("replyTime", comment.getUpdateTime());

            log.info("修改商家回复成功，评论ID: {}", commentId);
            return Result.success(data);

        } catch (CommentException e) {
            log.error("修改商家回复失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("修改商家回复异常", e);
            return Result.fail("修改回复失败，请重试");
        }
    }

    /**
     * 删除商家回复
     */
    @Override
    @Transactional
    public Result deleteReply(Long commentId) {
        try {
            // 验证评论权限
            ShopComment comment = validateCommentPermission(commentId);

            // 检查是否有回复
            if (comment.getReply() == null || comment.getReply().trim().isEmpty()) {
                throw new CommentException("该评论尚未回复，无法删除");
            }

            // 删除回复内容
            comment.setReply(null);
            comment.setUpdateTime(LocalDateTime.now());

            int updated = shopCommentMapper.updateById(comment);
            if (updated == 0) {
                throw new CommentException("删除回复失败，请重试");
            }

            log.info("删除商家回复成功，评论ID: {}", commentId);
            return Result.success();

        } catch (CommentException e) {
            log.error("删除商家回复失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("删除商家回复异常", e);
            return Result.fail("删除回复失败，请重试");
        }
    }

    /**
     * 获取评论统计数据
     */
    @Override
    public Result getStatistics() {
        try {
            // 获取当前商家店铺ID
            Long currentShopId = getCurrentShopId();
            log.info("开始获取评论统计数据，店铺ID: {}", currentShopId);

            // 查询总评论数
            QueryWrapper<ShopComment> totalWrapper = new QueryWrapper<>();
            totalWrapper.eq("shop_id", currentShopId);
            Long totalComments = shopCommentMapper.selectCount(totalWrapper).longValue();
            log.debug("总评论数: {}", totalComments);

            // 查询平均评分 - 使用更稳定的方式
            Double averageRating = 0.0;
            if (totalComments > 0) {
                QueryWrapper<ShopComment> avgWrapper = new QueryWrapper<>();
                avgWrapper.eq("shop_id", currentShopId);
                avgWrapper.select("rating");
                List<ShopComment> comments = shopCommentMapper.selectList(avgWrapper);

                if (!comments.isEmpty()) {
                    double sum = comments.stream().mapToInt(ShopComment::getRating).sum();
                    averageRating = sum / comments.size();
                    averageRating = Math.round(averageRating * 10.0) / 10.0; // 保留一位小数
                }
            }
            log.debug("平均评分: {}", averageRating);

            // 查询评分分布 - 转换为前端期望格式
            MerchantCommentStatisticsDTO.RatingDistribution ratingDistribution =
                new MerchantCommentStatisticsDTO.RatingDistribution();

            for (int i = 1; i <= 5; i++) {
                QueryWrapper<ShopComment> ratingWrapper = new QueryWrapper<>();
                ratingWrapper.eq("shop_id", currentShopId);
                ratingWrapper.eq("rating", i);
                Long count = shopCommentMapper.selectCount(ratingWrapper).longValue();

                // 根据评分设置对应字段
                switch (i) {
                    case 1: ratingDistribution.setRating1(count); break;
                    case 2: ratingDistribution.setRating2(count); break;
                    case 3: ratingDistribution.setRating3(count); break;
                    case 4: ratingDistribution.setRating4(count); break;
                    case 5: ratingDistribution.setRating5(count); break;
                }
            }
            log.debug("评分分布: 1星={}, 2星={}, 3星={}, 4星={}, 5星={}",
                ratingDistribution.getRating1(), ratingDistribution.getRating2(),
                ratingDistribution.getRating3(), ratingDistribution.getRating4(),
                ratingDistribution.getRating5());

            // 查询未回复数量
            QueryWrapper<ShopComment> pendingWrapper = new QueryWrapper<>();
            pendingWrapper.eq("shop_id", currentShopId);
            pendingWrapper.and(wrapper -> wrapper.isNull("reply").or().eq("reply", ""));
            Long unrepliedCount = shopCommentMapper.selectCount(pendingWrapper).longValue();
            log.debug("未回复数量: {}", unrepliedCount);

            // 计算回复率 - 修复计算逻辑，返回小数形式
            Double replyRate = 0.0;
            if (totalComments > 0) {
                Long repliedCount = totalComments - unrepliedCount;
                replyRate = repliedCount.doubleValue() / totalComments; // 不乘100，前端会处理
                replyRate = Math.round(replyRate * 1000.0) / 1000.0; // 保留三位小数
            }
            log.debug("回复率: {} (小数形式)", replyRate);

            // 构建统计数据
            MerchantCommentStatisticsDTO statistics = new MerchantCommentStatisticsDTO();
            statistics.setTotalComments(totalComments);
            statistics.setAverageRating(averageRating);
            statistics.setRatingDistribution(ratingDistribution);
            statistics.setUnrepliedCount(unrepliedCount);
            statistics.setReplyRate(replyRate);

            log.info("获取评论统计数据成功，店铺ID: {}, 总评论: {}, 平均评分: {}, 回复率: {}",
                currentShopId, totalComments, averageRating, replyRate);
            return Result.success(statistics);

        } catch (CommentException e) {
            log.error("获取评论统计数据失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取评论统计数据异常", e);
            return Result.fail("获取统计数据失败，请重试");
        }
    }

    /**
     * 举报不当评论
     */
    @Override
    @Transactional
    public Result reportComment(Long commentId, MerchantCommentReportDTO reportDTO) {
        try {
            // 验证评论权限
            validateCommentPermission(commentId);

            // 获取当前商家ID
            MerchantDTO merchant = MerchantHolder.getMerchant();
            if (merchant == null) {
                throw new CommentException("商家未登录");
            }

            // 检查是否已举报过
            QueryWrapper<CommentReport> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("comment_id", commentId);
            checkWrapper.eq("reporter_id", merchant.getId());
            CommentReport existingReport = commentReportMapper.selectOne(checkWrapper);
            if (existingReport != null) {
                throw new CommentException("您已举报过该评论，请勿重复举报");
            }

            // 创建举报记录
            CommentReport report = new CommentReport();
            report.setCommentId(commentId);
            report.setReporterId(merchant.getId());
            report.setReason(reportDTO.getReason());
            report.setStatus(0); // 0=待处理
            report.setCreateTime(LocalDateTime.now());
            report.setUpdateTime(LocalDateTime.now());

            int inserted = commentReportMapper.insert(report);
            if (inserted == 0) {
                throw new CommentException("举报失败，请重试");
            }

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("reportId", report.getId());
            data.put("status", "已提交举报，等待管理员处理");

            log.info("举报评论成功，评论ID: {}, 举报ID: {}", commentId, report.getId());
            return Result.success(data);

        } catch (CommentException e) {
            log.error("举报评论失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("举报评论异常", e);
            return Result.fail("举报失败，请重试");
        }
    }

    /**
     * 获取当前商家店铺ID
     */
    private Long getCurrentShopId() {
        MerchantDTO merchant = MerchantHolder.getMerchant();
        if (merchant == null) {
            throw new CommentException("商家未登录");
        }
        if (merchant.getShopId() == null) {
            throw new CommentException("商家暂无店铺信息");
        }
        return merchant.getShopId();
    }

    /**
     * 验证评论权限
     */
    private ShopComment validateCommentPermission(Long commentId) {
        if (commentId == null) {
            throw new CommentException("评论ID不能为空");
        }

        // 获取当前商家店铺ID
        Long currentShopId = getCurrentShopId();

        // 查询评论
        ShopComment comment = shopCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new CommentException("评论不存在");
        }

        // 验证权限
        if (!comment.getShopId().equals(currentShopId)) {
            throw new CommentException("无权限操作该评论");
        }

        return comment;
    }

    /**
     * 转换为列表DTO
     */
    private MerchantCommentListDTO convertToListDTO(ShopComment comment) {
        MerchantCommentListDTO dto = BeanUtil.copyProperties(comment, MerchantCommentListDTO.class);

        // 查询用户信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            dto.setUserNickname(user.getNickName());
            dto.setUserIcon(user.getIcon());
        }

        // 设置回复时间（如果有回复的话，使用更新时间作为回复时间）
        if (comment.getReply() != null && !comment.getReply().trim().isEmpty()) {
            dto.setReplyTime(comment.getUpdateTime());
        }

        return dto;
    }

    /**
     * 验证店铺归属
     */
    private void validateShopOwnership(Long merchantId, Long shopId) {
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", shopId).eq("merchant_id", merchantId);

        Shop shop = shopService.getOne(queryWrapper);
        if (shop == null) {
            throw new CommentException("店铺不存在或无权限访问");
        }
    }

    /**
     * 获取商家所有店铺ID列表
     */
    private List<Long> getMerchantShopIds(Long merchantId) {
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId).select("id");

        List<Shop> shops = shopService.list(queryWrapper);
        return shops.stream().map(Shop::getId).collect(Collectors.toList());
    }

    /**
     * 创建空的分页结果
     */
    private Map<String, Object> createEmptyPageResult() {
        Map<String, Object> data = new HashMap<>();
        data.put("records", new ArrayList<>());
        data.put("total", 0L);
        data.put("current", 1L);
        data.put("size", 10L);
        return data;
    }
}
