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

    /**
     * 创建并配置Redisson客户端Bean
     *
     * 该方法使用单节点服务器配置，配置Redis服务器地址和密码，最终创建RedissonClient实例。
     * 适用于需要与Redis服务进行交互的场景。
     *
     * @return RedissonClient 返回配置好的Redisson客户端实例，用于操作Redis服务
     */
    @Bean
    public RedissonClient redissonClient(){
        // 初始化Redisson配置对象并配置单节点模式
        Config config = new Config();
        // 配置单节点Redis服务器地址和认证密码
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");

        // 根据配置创建并返回Redisson客户端实例
        return Redisson.create(config);
    }

}