package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.AdminLoginDTO;
import com.hmdp.dto.AdminDTO;
import com.hmdp.entity.Admin;

/**
 * <p>
 * 管理员用户表 服务类
 * </p>
 *
 * @author Qitian Dasheng
 * @since 2024-07-31
 */
public interface IAdminService extends IService<Admin> {

    /**
     * 管理员登录
     * @param loginDTO 登录参数
     * @return 管理员信息及token
     */
    AdminDTO login(AdminLoginDTO loginDTO);
    
    /**
     * 获取管理员信息
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    AdminDTO getAdminInfo(Object adminId);
} 