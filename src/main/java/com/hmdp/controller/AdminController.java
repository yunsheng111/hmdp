package com.hmdp.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hmdp.common.Result;
import com.hmdp.dto.AdminDTO;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.service.IAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 管理员认证控制器
 * </p>
 *
 * @author Qitian Dasheng
 * @since 2024-07-31
 */
@Slf4j
@Api(tags = "管理员认证接口")
@RestController
@RequestMapping("/admin")
@Validated // 开启方法参数校验
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @ApiOperation("管理员登录")
    @PostMapping("/auth/login")
    public Result login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        log.info("Admin login attempt for username: {}", loginDTO.getUsername());
        try {
            AdminDTO adminDTO = adminService.login(loginDTO);
            return Result.success(adminDTO);
        } catch (RuntimeException e) {
            log.error("Admin login failed for username: {}", loginDTO.getUsername(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    @ApiOperation("管理员登出")
    @PostMapping("/auth/logout")
    public Result logout() {
        StpUtil.logout(); // 当前会话登出
        // 如果需要指定账户ID登出： StpUtil.logout(loginId);
        // 如果需要指定Token登出： StpUtil.logoutByTokenValue(tokenValue);
        return Result.success("登出成功");
    }
    
    @ApiOperation("获取当前管理员信息")
    @GetMapping("/info")
    public Result info() {
        log.info("获取管理员信息，ID: {}", StpUtil.getLoginIdAsString());
        try {
            // 获取当前登录的管理员ID
            Object loginId = StpUtil.getLoginId();
            // 调用服务获取管理员详细信息
            AdminDTO adminInfo = adminService.getAdminInfo(loginId);
            return Result.success(adminInfo);
        } catch (Exception e) {
            log.error("获取管理员信息失败", e);
            return Result.fail("获取管理员信息失败");
        }
    }
} 