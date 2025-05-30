package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员实体类
 */
@Data
@NoArgsConstructor
@TableName("tb_admin_user")
public class Admin {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 管理员用户名
     */
    private String username;

    /**
     * 密码（存储的是密码的哈希值）
     */
    private String password;



    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 账号状态：0-正常，1-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记 (0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer deleted;

    /**
     * 角色列表，非数据库字段，用于业务处理
     */
    @TableField(exist = false)
    private List<String> roleList;
} 