package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.service.IAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 管理员控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private IAdminService adminService;

    /**
     * 管理员登录
     * @param loginDTO 登录表单
     * @return Result
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        return adminService.login(loginDTO);
    }

    /**
     * 管理员退出登录
     * @param request 请求
     * @return Result
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return adminService.logout(request);
    }

    /**
     * 获取当前登录管理员信息
     * @return Result
     */
    @GetMapping("/info")
    public Result getCurrentAdmin() {
        return adminService.getCurrentAdmin();
    }
    
    /**
     * 获取系统统计数据
     * @return Result
     */
    @GetMapping("/stats/summary")
    public Result getStatsSummary() {
        // 这里返回模拟数据，真实环境下应该从服务层获取
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", 1258);
        stats.put("merchantCount", 356);
        stats.put("todayOrders", 89);
        stats.put("pendingReports", 12);
        
        return Result.success(stats);
    }
} 