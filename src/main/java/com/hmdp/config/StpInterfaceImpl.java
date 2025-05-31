package com.hmdp.config;

import cn.dev33.satoken.stp.StpInterface;
import com.hmdp.entity.AdminRole;
import com.hmdp.entity.AdminUserRole;
import com.hmdp.mapper.AdminRoleMapper;
import com.hmdp.mapper.AdminUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private AdminUserRoleMapper adminUserRoleMapper;

    @Autowired
    private AdminRoleMapper adminRoleMapper;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 管理员暂不使用权限码，直接返回空列表
        // 后续可根据业务需求扩展
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        Long adminId;
        try {
            adminId = Long.valueOf(loginId.toString());
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }

        // 1. 根据 adminId 查询用户拥有的角色ID列表
        List<AdminUserRole> userRoles = adminUserRoleMapper.selectList(
                new QueryWrapper<AdminUserRole>().eq("admin_user_id", adminId)
        );

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream()
                .map(AdminUserRole::getRoleId)
                .collect(Collectors.toList());

        // 2. 根据角色ID列表查询角色信息，并提取角色编码(code)
        List<AdminRole> roles = adminRoleMapper.selectBatchIds(roleIds);

        if (roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(AdminRole::getCode) // 假设 AdminRole 中有 getCode() 方法返回角色标识字符串
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toList());
    }
} 