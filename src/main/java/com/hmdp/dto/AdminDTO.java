package com.hmdp.dto;

import lombok.Data;
import java.util.List;

/**
 * 管理员信息DTO（用于前端展示）
 */
@Data
public class AdminDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 登录凭证Token
     */
    private String token;
}
