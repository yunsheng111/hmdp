# 商家登录认证问题修复记录

**创建日期**: [2024-08-04 15:30:00 +08:00]

## 问题描述

在商家登录后点击店铺管理页面时，系统显示"登录已过期"和"获取商铺信息失败"的错误提示，导致用户无法正常访问店铺管理功能。

## 问题原因分析

通过分析前端和后端代码，发现了以下问题：

1. 前端（merchant-common.js）在请求拦截器中只设置了`merchant-token`请求头，但未设置`authorization`请求头
2. 后端（MerchantRefreshTokenInterceptor.java）在拦截器中只检查`merchant-token`请求头，未检查`authorization`请求头
3. 导致商家登录状态无法正确传递，系统认为用户未登录

## 解决方案

### 前端修改（merchant-common.js）

修改请求拦截器，同时设置`merchant-token`和`authorization`请求头：

```javascript
// request拦截器，将商家token放入头中
merchantAxios.interceptors.request.use(
  config => {
    // 每次请求前重新获取最新token
    merchantToken = sessionStorage.getItem("merchantToken");
    if(merchantToken) {
      // 使用两种header确保兼容性
      config.headers['merchant-token'] = merchantToken;
      // 添加authorization头，与后端认证机制保持一致
      config.headers['authorization'] = merchantToken;
    }
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)
```

### 后端修改（MerchantRefreshTokenInterceptor.java）

修改拦截器的preHandle方法，优先使用`merchant-token`头，如果不存在则尝试使用`authorization`头：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 1.获取请求头中的token，优先使用merchant-token，如果没有则尝试使用authorization
    String token = request.getHeader("merchant-token");
    if (StrUtil.isBlank(token)) {
        // 尝试从authorization头获取
        token = request.getHeader("authorization");
        log.debug("从authorization头获取token: {}", token);
    } else {
        log.debug("从merchant-token头获取token: {}", token);
    }
    
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
```

## 修复效果

通过同时在前端发送`merchant-token`和`authorization`请求头，并在后端兼容处理两种头信息，解决了商家登录状态无法正确传递的问题。现在商家登录后可以正常访问店铺管理功能，不再出现"登录已过期"的错误提示。

## 后续建议

1. 统一认证头命名规范，避免使用多个不同的头名称
2. 考虑引入统一的认证框架（如Spring Security或Sa-Token）来处理认证逻辑
3. 增加前端请求失败的重试机制，提高系统容错性
4. 完善日志记录，便于问题追踪和分析 