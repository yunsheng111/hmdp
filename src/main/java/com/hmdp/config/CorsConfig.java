package com.hmdp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 
 * @Description CORS跨域配置类
 * @date 2024年11月06日 上午 10:46
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "http://localhost:8080",
                    "http://127.0.0.1:8080",
                    "http://localhost:8081",
                    "http://127.0.0.1:8081",
                    "http://localhost",
                    "http://127.0.0.1"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Sa-Token")
                .allowCredentials(true)
                .maxAge(168000);
    }
}
