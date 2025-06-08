package com.hmdp.config;
import com.hmdp.common.MerchantLoginInterceptor;
import com.hmdp.common.MerchantRefreshTokenInterceptor;
import com.hmdp.common.RefreshTokenInterceptor;
import com.hmdp.common.LoginInterceptor;
import com.hmdp.common.AdminLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.NonNull;
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
    public void addInterceptors(@NonNull InterceptorRegistry registry){
        // 注册登录拦截器 - 只拦截需要普通用户登录的路径
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/user/**", "/blog/**", "/blog-comments/**", "/follow/**", "/order/**")
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/blog/hot",
                        "/blog-comments/blog/**"  // 允许查看评论，不需要登录
                ).order(1);
        // 注册商家登录拦截器
        registry.addInterceptor(new MerchantLoginInterceptor())
                .addPathPatterns("/merchant/**", "/product/**")
                .excludePathPatterns(
                        "/merchant/code",
                        "/merchant/login",
                        "/merchant/register",
                        "/product/categories/**",
                        "/product/shop/**"
                ).order(2);
        // 注册管理员登录拦截器
        registry.addInterceptor(new AdminLoginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/auth/login",
                        "/admin/auth/logout"
                ).order(3);
        // 注册刷新拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(redisTemplate)).addPathPatterns("/**").order(0);
        // 注册商家Token刷新拦截器
        registry.addInterceptor(new MerchantRefreshTokenInterceptor(redisTemplate)).addPathPatterns("/**").order(-1);
    }
    /**
     * 设置静态资源映射
     * @param registry
     */
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
    }

    /**
     * 配置消息转换器
     * @param converters
     */
    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        // 使用UTF-8字符集，解决中文乱码问题
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(0, converter);
    }
}
