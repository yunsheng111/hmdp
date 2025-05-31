package com.hmdp.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.AdminDTO;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.entity.Admin;
import com.hmdp.mapper.AdminMapper;
import com.hmdp.service.IAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 管理员用户表 服务实现类
 * </p>
 *
 * @author Qitian Dasheng
 * @since 2024-07-31
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Override
    public AdminDTO login(AdminLoginDTO loginDTO) {
        // 1. 根据用户名查询管理员
        Admin admin = query().eq("username", loginDTO.getUsername()).one();

        // 2. 判断用户是否存在
        if (admin == null) {
            // 用户不存在，抛出异常或返回错误信息
            log.warn("用户名不存在: {}", loginDTO.getUsername());
            throw new RuntimeException("用户名或密码错误"); // 建议使用自定义异常
        }

        // 3. 判断账户状态是否正常
        if (admin.getStatus() != 0) {
            // 账户被禁用
            log.warn("账户已被禁用: {}", loginDTO.getUsername());
            throw new RuntimeException("账户已被禁用"); // 建议使用自定义异常
        }

        // 4. 校验密码
        // !!! 安全警告: 当前为明文密码比较，生产环境必须使用强哈希算法 (如BCrypt) !!!
        if (!loginDTO.getPassword().equals(admin.getPassword())) {
            // 密码错误
            log.warn("密码错误: {}", loginDTO.getUsername());
            throw new RuntimeException("用户名或密码错误"); // 建议使用自定义异常
        }

        // 5. 登录成功，生成Sa-Token
        StpUtil.login(admin.getId());
        log.info("Admin user {} logged in successfully.", admin.getUsername());

        // 6. 准备AdminDTO返回
        AdminDTO adminDTO = new AdminDTO();
        BeanUtils.copyProperties(admin, adminDTO); // 复制基本属性
        adminDTO.setToken(StpUtil.getTokenValue()); // 设置Sa-Token值
        
        // 7. 获取角色信息 (通过StpUtil.getRoleList, StpInterfaceImpl会处理)
        List<String> roleList = StpUtil.getRoleList(admin.getId());
        adminDTO.setRoles(roleList);

        return adminDTO;
    }
    
    @Override
    public AdminDTO getAdminInfo(Object adminId) {
        // 1. 根据ID查询管理员
        Admin admin = getById(adminId.toString());
        if (admin == null) {
            log.warn("管理员ID不存在: {}", adminId);
            throw new RuntimeException("管理员信息不存在");
        }
        
        // 2. 转换为DTO
        AdminDTO adminDTO = new AdminDTO();
        BeanUtils.copyProperties(admin, adminDTO);
        
        // 3. 设置角色信息
        List<String> roleList = StpUtil.getRoleList(adminId);
        adminDTO.setRoles(roleList);
        
        // 4. 不需要设置token，因为这个方法是在已登录的情况下调用的
        
        return adminDTO;
    }
} 