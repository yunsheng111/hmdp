package com.hmdp.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 管理员登录表单数据传输对象
 */
@Data
public class AdminLoginDTO {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
} 