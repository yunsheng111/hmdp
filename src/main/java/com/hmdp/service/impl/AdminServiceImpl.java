package com.hmdp.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.AdminDTO;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.entity.Admin;
import com.hmdp.entity.AdminRole;
import com.hmdp.entity.AdminUserRole;
import com.hmdp.mapper.AdminMapper;
import com.hmdp.mapper.AdminRoleMapper;
import com.hmdp.mapper.AdminUserRoleMapper;
import com.hmdp.service.IAdminService;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 管理员服务实现类
 * </p>
 *
 * @author yate
 * @since 2024-07-27
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Resource
    private AdminMapper adminMapper;
    
    @Resource
    private AdminRoleMapper adminRoleMapper;
    
    @Resource
    private AdminUserRoleMapper adminUserRoleMapper;

    private static final Integer ADMIN_STATUS_DISABLED = 1; // 禁用状态
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public Result login(AdminLoginDTO adminLoginDTO) {
        try {
            log.info("管理员登录尝试，用户名: {}", adminLoginDTO.getUsername());
            
            // 1. 参数校验
            String username = adminLoginDTO.getUsername();
            String password = adminLoginDTO.getPassword();
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                log.warn("管理员登录失败，用户名: {}, 原因: {}", username, "用户名或密码为空");
                return Result.fail("用户名或密码不能为空");
            }

            // 2. 根据用户名查询管理员
            Admin admin = null;
            try {
                admin = query().eq("username", username).one();
                log.info("查询用户 {} 结果: {}", username, (admin != null ? "存在" : "不存在"));
            } catch (Exception e) {
                log.error("查询管理员信息失败: {}", e.getMessage(), e);
                return Result.fail("系统异常，请稍后再试");
            }

            // 3. 判断用户是否存在
            if (admin == null) {
                log.warn("管理员登录失败，用户名: {}, 原因: {}", username, "用户名不存在");
                return Result.fail("用户名或密码错误");
            }

            // 4. 判断账号状态是否正常
            if (ADMIN_STATUS_DISABLED.equals(admin.getStatus())) {
                log.warn("管理员登录失败，用户名: {}, 原因: {}", username, "账号已被禁用");
                return Result.fail("账号已被禁用");
            }

            // 5. 校验密码（使用注入的PasswordEncoder）
            boolean passwordMatch = false;
            try {
                passwordMatch = passwordEncoder.matches(password, admin.getPassword());
                log.info("用户 {} 密码校验结果: {}", username, (passwordMatch ? "正确" : "错误"));
            } catch (Exception e) {
                log.error("密码校验失败: {}", e.getMessage(), e);
                return Result.fail("系统异常，请稍后再试");
            }
            
            if (!passwordMatch) {
                log.warn("管理员登录失败，用户名: {}, 原因: {}", username, "密码错误");
                return Result.fail("用户名或密码错误");
            }

            // 6. 登录成功，生成Token (使用Sa-Token)
            String token = null;
            try {
                StpUtil.login(admin.getId());
                token = StpUtil.getTokenValue();
                log.info("生成token成功: {}", token);
            } catch (Exception e) {
                log.error("生成Token失败: {}", e.getMessage(), e);
                return Result.fail("系统异常，请稍后再试");
            }
            
            // 7. 获取管理员角色列表
            List<String> roles = null;
            try {
                roles = adminMapper.findRolesByAdminId(admin.getId());
                log.info("获取用户 {} 角色列表: {}", username, roles);
            } catch (Exception e) {
                log.error("获取角色列表失败: {}", e.getMessage(), e);
                // 继续执行，使用默认角色
            }
            
            if (roles == null || roles.isEmpty()) {
                // 如果没有角色，使用默认角色
                roles = Collections.singletonList("admin");
                log.info("使用默认角色: {}", roles);
            }

            // 8. 封装AdminDTO返回
            AdminDTO adminDTO = new AdminDTO();
            BeanUtils.copyProperties(admin, adminDTO); // 拷贝基础属性
            adminDTO.setRoles(roles); // 设置角色列表
            adminDTO.setToken(token);

            log.info("管理员 {} 登录成功", admin.getUsername());
            return Result.success(adminDTO);
        } catch (Exception e) {
            log.error("管理员登录过程中发生未预期的异常: {}", e.getMessage(), e);
            return Result.fail("服务器异常，请稍后再试");
        }
    }
    
    
    /**
     * 检查并创建默认管理员
     */
    private void createDefaultAdminIfNotExists() {
        try {
            // 检查是否已存在admin用户
            Admin existingAdmin = adminMapper.selectOne(
                    new QueryWrapper<Admin>().eq("username", DEFAULT_USERNAME)
            );
            
            if (existingAdmin != null) {
                log.info("默认管理员 [{}] 已存在，无需创建", DEFAULT_USERNAME);
                return;
            }
            
            log.info("开始创建默认管理员: {}", DEFAULT_USERNAME);
            
            // 1. 查找或创建超级管理员角色
            Long roleId = findOrCreateSuperAdminRole();
            
            // 2. 创建管理员用户
            Admin admin = new Admin();
            admin.setUsername(DEFAULT_USERNAME);
            // 使用BCryptPasswordEncoder加密密码
            admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            admin.setStatus(0); // 0-正常状态
            admin.setDeleted(0); // 0-未删除
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            
            // 3. 保存管理员
            adminMapper.insert(admin);
            log.info("成功创建默认管理员: {}，ID: {}", DEFAULT_USERNAME, admin.getId());
            
            // 4. 关联管理员和角色
            AdminUserRole adminUserRole = new AdminUserRole();
            adminUserRole.setAdminId(admin.getId());
            adminUserRole.setRoleId(roleId);
            adminUserRoleMapper.insert(adminUserRole);
            log.info("成功关联默认管理员和超级管理员角色");
            
            log.info("默认管理员 [{}] 创建成功，初始密码: {}", DEFAULT_USERNAME, DEFAULT_PASSWORD);
        } catch (Exception e) {
            // 捕获所有异常，记录日志但不影响应用启动
            log.error("创建默认管理员失败，应用将继续运行，请手动创建管理员账户。错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 查找或创建超级管理员角色
     * @return 角色ID
     */
    private Long findOrCreateSuperAdminRole() {
        try {
            // 先查找是否已存在超级管理员角色
            QueryWrapper<AdminRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", "SUPER_ADMIN");
            AdminRole existingRole = adminRoleMapper.selectOne(queryWrapper);
            
            if (existingRole != null) {
                log.info("超级管理员角色已存在，角色ID: {}", existingRole.getId());
                return existingRole.getId();
            }
            
            // 创建超级管理员角色
            AdminRole role = new AdminRole();
            role.setName("超级管理员");
            role.setCode("SUPER_ADMIN");
            role.setDescription("系统最高权限角色，拥有所有操作权限");
            role.setStatus(0); // 0-正常状态
            role.setDeleted(0); // 0-未删除
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            
            adminRoleMapper.insert(role);
            log.info("成功创建超级管理员角色，角色ID: {}", role.getId());
            
            return role.getId();
        } catch (Exception e) {
            log.error("创建超级管理员角色失败: {}", e.getMessage(), e);
            // 返回一个默认值或抛出自定义异常
            throw new RuntimeException("创建超级管理员角色失败", e);
        }
    }

    @Override
    public Result logout(HttpServletRequest request) {
        // 检查是否已登录
        if (StpUtil.isLogin()) {
            Long adminId = StpUtil.getLoginIdAsLong();
            Admin admin = getById(adminId);
            if (admin != null) {
                log.info("管理员 {} 退出登录", admin.getUsername());
            }
        }
        
        // 使用Sa-Token登出
        StpUtil.logout();
        return Result.success("登出成功");
    }

    @Override
    public Result getCurrentAdmin() {
        // 检查是否已登录
        if (!StpUtil.isLogin()) {
            log.warn("获取当前管理员信息失败，原因: 未登录");
            return Result.fail("未登录");
        }
        
        // 获取当前登录的管理员ID
        Long adminId = StpUtil.getLoginIdAsLong();
        
        // 查询管理员信息
        Admin admin = getById(adminId);
        if (admin == null) {
            log.warn("获取当前管理员信息失败，原因: 账户不存在，ID: {}", adminId);
            StpUtil.logout(); // 登录ID无效，强制登出
            return Result.fail("账户不存在");
        }
        
        // 获取管理员角色列表
        List<String> roles = adminMapper.findRolesByAdminId(admin.getId());
        if (roles == null || roles.isEmpty()) {
            // 如果没有角色，使用默认角色
            roles = Collections.singletonList("admin");
        }
        
        // 转换为DTO
        AdminDTO adminDTO = new AdminDTO();
        BeanUtils.copyProperties(admin, adminDTO);
        adminDTO.setRoles(roles); // 设置角色列表
        
        log.info("获取管理员 {} 信息成功", admin.getUsername());
        return Result.success(adminDTO);
    }

    /**
     * 初始化方法，在服务启动时自动执行
     */
    @PostConstruct
    public void init() {
        createDefaultAdminIfNotExists();
    }
}