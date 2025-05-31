package com.hmdp.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hmdp.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 管理员统计数据控制器
 * </p>
 *
 * @author Qitian Dasheng
 * @since 2024-07-31
 */
@Slf4j
@Api(tags = "管理员统计接口")
@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    @ApiOperation("获取统计数据摘要")
    @GetMapping("/summary")
    public Result summary() {
        // 拦截器已经验证了登录状态，这里直接获取管理员ID
        log.info("管理员 {} 请求统计数据摘要", StpUtil.getLoginIdAsString());
        
        try {
            // 这里应该从各个服务获取真实数据
            // 为简化演示，使用模拟数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("userCount", 1258);
            stats.put("merchantCount", 356);
            stats.put("todayOrders", 89);
            stats.put("pendingReports", 12);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.fail("获取统计数据失败");
        }
    }
} 