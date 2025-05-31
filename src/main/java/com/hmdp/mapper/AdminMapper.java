package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 管理员用户表 Mapper 接口
 * </p>
 *
 * @author Qitian Dasheng
 * @since 2024-07-31
 */
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据管理员ID查询角色代码列表
     *
     * @param adminId 管理员ID
     * @return 角色代码列表
     */
    List<String> findRolesByAdminId(@Param("adminId") Long adminId);
} 