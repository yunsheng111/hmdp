package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.entity.Admin;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 管理员服务接口
 * </p>
 *
 * @author yate
 * @since 2024-07-29
 */
public interface IAdminService extends IService<Admin> {

    /**
     * 管理员登录
     *
     * @param loginDTO 登录表单
     * @return Result 包含登录结果，成功时可能包含token
     */
    Result login(AdminLoginDTO loginDTO);

    /**
     * 管理员退出登录
     *
     * @param request HTTP请求，用于会话管理
     * @return Result 操作结果
     */
    Result logout(HttpServletRequest request);

    /**
     * 获取当前登录的管理员信息
     *
     * @return Result 包含管理员信息
     */
    Result getCurrentAdmin();
} 