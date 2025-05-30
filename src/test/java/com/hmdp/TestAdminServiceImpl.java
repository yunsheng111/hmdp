package com.hmdp;

import cn.dev33.satoken.stp.StpUtil;
import com.hmdp.common.Result;
import com.hmdp.dto.AdminDTO;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.entity.Admin;
import com.hmdp.mapper.AdminMapper;
import com.hmdp.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AdminServiceImpl 单元测试类
 */
@ExtendWith(MockitoExtension.class)
public class TestAdminServiceImpl {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    private AdminLoginDTO validLoginDTO;
    private Admin validAdmin;

    @BeforeEach
    public void setup() {
        // 设置有效的登录DTO
        validLoginDTO = new AdminLoginDTO();
        validLoginDTO.setUsername("admin");
        validLoginDTO.setPassword("password");

        // 设置有效的Admin对象
        validAdmin = new Admin();
        validAdmin.setId(1L);
        validAdmin.setUsername("admin");
        validAdmin.setPassword("hashedPassword");
        validAdmin.setStatus(0); // 正常状态
        validAdmin.setAvatar("avatar.jpg");
    }

    /**
     * 测试成功登录场景
     */
    @Test
    public void testLoginSuccess() {
        // 模拟数据库查询结果
        when(adminService.query().eq("username", "admin").one()).thenReturn(validAdmin);
        
        // 模拟密码验证
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        
        // 模拟角色查询
        when(adminMapper.findRolesByAdminId(1L)).thenReturn(Arrays.asList("admin", "editor"));

        // 模拟Sa-Token
        try (MockedStatic<StpUtil> mockedStpUtil = Mockito.mockStatic(StpUtil.class)) {
            mockedStpUtil.when(() -> StpUtil.login(anyLong())).thenReturn(null);
            mockedStpUtil.when(StpUtil::getTokenValue).thenReturn("mock-token-value");

            // 执行登录方法
            Result result = adminService.login(validLoginDTO);

            // 验证结果
            assertTrue(result.getSuccess());
            assertNotNull(result.getData());
            AdminDTO adminDTO = (AdminDTO) result.getData();
            assertEquals(1L, adminDTO.getId());
            assertEquals("admin", adminDTO.getUsername());
            assertEquals("mock-token-value", adminDTO.getToken());
            assertEquals(2, adminDTO.getRoles().size());
        }
    }

    /**
     * 测试用户名不存在的场景
     */
    @Test
    public void testLoginWithNonExistentUsername() {
        // 模拟数据库查询结果：找不到用户
        when(adminService.query().eq("username", "admin").one()).thenReturn(null);

        // 执行登录方法
        Result result = adminService.login(validLoginDTO);

        // 验证结果
        assertFalse(result.getSuccess());
        assertEquals("用户名或密码错误", result.getErrorMsg());
    }

    /**
     * 测试账户被禁用的场景
     */
    @Test
    public void testLoginWithDisabledAccount() {
        // 设置账户状态为禁用
        validAdmin.setStatus(1);
        
        // 模拟数据库查询结果
        when(adminService.query().eq("username", "admin").one()).thenReturn(validAdmin);

        // 执行登录方法
        Result result = adminService.login(validLoginDTO);

        // 验证结果
        assertFalse(result.getSuccess());
        assertEquals("账号已被禁用", result.getErrorMsg());
    }

    /**
     * 测试密码错误的场景
     */
    @Test
    public void testLoginWithIncorrectPassword() {
        // 模拟数据库查询结果
        when(adminService.query().eq("username", "admin").one()).thenReturn(validAdmin);
        
        // 模拟密码验证失败
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(false);

        // 执行登录方法
        Result result = adminService.login(validLoginDTO);

        // 验证结果
        assertFalse(result.getSuccess());
        assertEquals("用户名或密码错误", result.getErrorMsg());
    }

    /**
     * 测试没有角色的场景（应使用默认角色）
     */
    @Test
    public void testLoginWithNoRoles() {
        // 模拟数据库查询结果
        when(adminService.query().eq("username", "admin").one()).thenReturn(validAdmin);
        
        // 模拟密码验证
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        
        // 模拟角色查询 - 返回空列表
        when(adminMapper.findRolesByAdminId(1L)).thenReturn(Collections.emptyList());

        // 模拟Sa-Token
        try (MockedStatic<StpUtil> mockedStpUtil = Mockito.mockStatic(StpUtil.class)) {
            mockedStpUtil.when(() -> StpUtil.login(anyLong())).thenReturn(null);
            mockedStpUtil.when(StpUtil::getTokenValue).thenReturn("mock-token-value");

            // 执行登录方法
            Result result = adminService.login(validLoginDTO);

            // 验证结果
            assertTrue(result.getSuccess());
            assertNotNull(result.getData());
            AdminDTO adminDTO = (AdminDTO) result.getData();
            assertEquals(1, adminDTO.getRoles().size());
            assertEquals("admin", adminDTO.getRoles().get(0)); // 应该使用默认角色
        }
    }
} 