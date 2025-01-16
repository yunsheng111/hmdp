package com.hmdp.utils;

public class RedisConstants {
    //登录验证码的key，手机号码作为key，验证码作为value，手机号码的前缀
    public static final String LOGIN_CODE_KEY = "login:code:";
    //验证码的有效期,2秒钟
    public static final Long LOGIN_CODE_TTL = 2L;
    //登录用户的key，token作为key，用户信息作为value，token的前缀
    public static final String LOGIN_USER_KEY = "login:token:";
    //登录用户的过期时间，单位为秒。其值为 36000，即表示登录用户的有效期是 10 小时（36000 秒）
    public static final Long LOGIN_USER_TTL = 36000L;

    //缓存店铺空值的有效期，单位为秒。其值为 2，即表示缓存空值的有效期是 2 秒
    public static final Long CACHE_NULL_TTL = 2L;

    //缓存店铺的非空值的有效期，单位为秒。其值为 30，即表示缓存非空值的有效期是 30 秒
    public static final Long CACHE_SHOP_TTL = 30L;

    // 缓存商铺的key，id作为key，商铺信息作为value，id的前缀
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    //互斥锁的key，id作为key，互斥锁作为value，id的前缀
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    //互斥锁的过期时间，单位为秒。其值为 10，即表示互斥锁的有效期是 10 秒
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
