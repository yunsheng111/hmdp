package com.hmdp.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员登录拦截器
 * 基于Sa-Token进行管理员身份验证
 */
@Slf4j
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 打印请求的URI，便于调试
        String requestURI = request.getRequestURI();
        log.debug("AdminLoginInterceptor处理请求: {}", requestURI);

        // 打印所有请求头，用于调试
        if (log.isDebugEnabled()) {
            log.debug("请求头信息:");
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                log.debug("{}={}", headerName, request.getHeader(headerName));
            }
        }

        try {
            // Sa-Token会自动从配置的地方（Header/Cookie）读取token
            // 直接检查当前请求是否已登录
            StpUtil.checkLogin();

            // 获取当前登录的管理员ID用于日志记录
            Object loginId = StpUtil.getLoginId();
            log.debug("Admin authentication successful for request: {}, adminId: {}",
                     requestURI, loginId);
            return true;
        } catch (NotLoginException e) {
            log.warn("Admin authentication failed for request: {}, error: {}",
                    requestURI, e.getMessage());
            // 设置401状态码
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"errorMsg\":\"请先登录\"}");
            return false;
        } catch (Exception e) {
            log.error("Admin authentication error for request: {}, error: {}",
                     requestURI, e.getMessage(), e);
            // 设置500状态码
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"errorMsg\":\"服务器内部错误\"}");
            return false;
        }
    }
}
