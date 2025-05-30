package com.hmdp.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.MerchantDTO;
import com.hmdp.utils.MerchantHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.MERCHANT_LOGIN_TOKEN_KEY;
import static com.hmdp.utils.RedisConstants.MERCHANT_LOGIN_TOKEN_TTL;

/**
 * 商家Token刷新拦截器
 */
@Slf4j
public class MerchantRefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public MerchantRefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader("merchant-token");
        log.debug("merchant-token: {}", token);
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 2.基于TOKEN获取redis中的商家
        String key = MERCHANT_LOGIN_TOKEN_KEY + token;
        Map<Object, Object> merchantMap = stringRedisTemplate.opsForHash().entries(key);
        // 3.判断商家是否存在
        if (merchantMap.isEmpty()) {
            return true;
        }
        // 4.将查询到的hash数据转为MerchantDTO
        MerchantDTO merchantDTO = BeanUtil.fillBeanWithMap(merchantMap, new MerchantDTO(), false);
        // 5.存在，保存商家信息到 ThreadLocal
        MerchantHolder.saveMerchant(merchantDTO);
        // 6.刷新token有效期
        stringRedisTemplate.expire(key, MERCHANT_LOGIN_TOKEN_TTL, TimeUnit.MINUTES);
        // 7.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除商家
        MerchantHolder.removeMerchant();
    }
} 