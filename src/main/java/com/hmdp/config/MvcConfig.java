package com.hmdp.config;

import com.hmdp.common.LoginInterceptor;
import com.hmdp.common.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @ Tool：IntelliJ IDEA
 * @ Author：云生
 * @ Date：2025-01-04-18:46
 * @ Version：1.0
 * @ Description：
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        // 注册登录拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/blog/hot",
                        "/shop/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/voucher/**",
                        "/doc.html/**"
                ).order(1);
        // 注册刷新拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(redisTemplate)).addPathPatterns("/**").order(0);
    }
    /**
     * 设置静态资源映射
     * @param registry
     */
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
    }

    /**
     * 配置消息转换器
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 使用UTF-8字符集，解决中文乱码问题
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(0, converter);
    }
}
