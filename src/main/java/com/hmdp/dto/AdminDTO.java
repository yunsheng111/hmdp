package com.hmdp.dto;

import lombok.Data;
import java.util.List;

/**
 * 管理员信息DTO (用于前端展示)
 */
@Data
public class AdminDTO {
    private Long id;
    private String username;
    private String avatar;
    private List<String> roles; // 角色编码列表
    private String token; // Sa-Token
} 