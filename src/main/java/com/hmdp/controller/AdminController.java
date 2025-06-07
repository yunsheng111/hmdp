package com.hmdp.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.Result;
import com.hmdp.dto.AdminDTO;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IAdminService;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

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
    
    @Resource
    private IUserService userService;
    
    @Resource
    private IUserInfoService userInfoService;

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
    @PostMapping("/logout")
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
    
    @ApiOperation("获取用户列表")
    @GetMapping("/users")
    public Result getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {
        
        log.info("获取用户列表，页码：{}，每页数量：{}，关键词：{}，状态：{}", page, size, keyword, status);
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            // 如果有关键词，搜索用户名或手机号
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(User::getNickName, keyword)
                        .or()
                        .like(User::getPhone, keyword);
            }
            
            // 如果指定了状态，添加状态过滤条件
            if (status != null) {
                queryWrapper.eq(User::getStatus, status);
            }
            
            // 创建分页对象
            Page<User> userPage = new Page<>(page, size);
            
            // 执行分页查询
            Page<User> result = userService.page(userPage, queryWrapper);
            
            // 构建返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            data.put("size", result.getSize());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.fail("获取用户列表失败");
        }
    }
    
    @ApiOperation("获取用户详情")
    @GetMapping("/users/{userId}/details")
    public Result getUserDetails(@PathVariable("userId") Long userId) {
        log.info("获取用户详情，用户ID：{}", userId);
        
        try {
            // 1. 获取用户基本信息
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            
            // 2. 获取用户详细信息
            UserInfo userInfo = userInfoService.getById(userId);
            
            // 3. 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("basicInfo", user);
            data.put("userInfo", userInfo);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户详情失败，用户ID：{}", userId, e);
            return Result.fail("获取用户详情失败");
        }
    }
    
    @ApiOperation("修改用户状态")
    @PutMapping("/users/{userId}/status")
    public Result updateUserStatus(
            @PathVariable("userId") Long userId,
            @RequestBody Map<String, Integer> statusMap) {
        
        Integer status = statusMap.get("status");
        log.info("修改用户状态，用户ID：{}，新状态：{}", userId, status);
        
        if (status == null) {
            return Result.fail("状态参数不能为空");
        }
        
        // 验证状态值是否有效
        if (status != 0 && status != 1) {
            return Result.fail("无效的状态值，只能是0（正常）或1（禁用）");
        }
        
        try {
            // 1. 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            
            // 2. 修改用户状态
            user.setStatus(status);
            boolean updated = userService.updateById(user);
            
            if (!updated) {
                return Result.fail("更新用户状态失败");
            }
            
            // 3. 返回成功结果
            String statusText = status == 0 ? "正常" : "禁用";
            return Result.success("用户状态已更新为" + statusText);
        } catch (Exception e) {
            log.error("修改用户状态失败，用户ID：{}，新状态：{}", userId, status, e);
            return Result.fail("修改用户状态失败");
        }
    }
} 