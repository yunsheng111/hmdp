package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ Tool：IntelliJ IDEA
 * @ Author：云生
 * @ Date：2025-02-20-3:35
 * @ Version：1.0
 * @ Description：
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}