package com.hmdp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全相关配置类
 */
@Configuration
public class SecurityConfig {

    /**
     * 提供BCryptPasswordEncoder的Bean，用于密码加密和校验
     * 
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 