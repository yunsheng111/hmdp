package com.hmdp.common;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员登录拦截器
 */
@Slf4j
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查管理员是否已登录
        if (!StpUtil.isLogin()) {
            log.debug("管理员未登录，请求被拦截");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"errorMsg\":\"未登录\"}");
            return false;
        }
        return true;
    }
} 