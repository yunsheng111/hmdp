package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员-角色关联实体类
 */
@Data
@NoArgsConstructor
@TableName("tb_admin_user_role")
public class AdminUserRole {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 管理员ID
     */
    @TableField("user_id")
    private Long adminId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 