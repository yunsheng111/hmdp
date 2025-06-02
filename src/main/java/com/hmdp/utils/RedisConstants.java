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

    // 商家登录验证码的key前缀
    public static final String MERCHANT_LOGIN_CODE_KEY = "merchant:login:code:";
    // 商家登录token的key前缀
    public static final String MERCHANT_LOGIN_TOKEN_KEY = "merchant:login:token:";
    // 商家登录token的过期时间，单位为分钟
    public static final Long MERCHANT_LOGIN_TOKEN_TTL = 1440L; // 24小时

    //缓存店铺空值的有效期，单位为分钟。其值为 5，即表示缓存空值的有效期是 5 分钟
    public static final Long CACHE_NULL_TTL = 5L;

    //缓存店铺的非空值的有效期，单位为分钟。其值为 30，即表示缓存非空值的有效期是 30 分钟
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
    
    // 博客已读标记的key前缀
    public static final String BLOG_READ_KEY = "blog:read:";
    
    // 新增：方案1所需的Redis Key前缀
    // 用户总未读数计数器key前缀
    public static final String TOTAL_UNREAD_COUNT_KEY_PREFIX = "total_unread:";
    
    // 用户对特定作者的未读数计数器key前缀
    public static final String AUTHOR_UNREAD_COUNT_KEY_PREFIX = "author_unread:";
    
    // 博客作者映射key前缀
    public static final String BLOG_AUTHOR_MAP_KEY_PREFIX = "blog_author:";
    
    // 已删除博客集合key
    public static final String DELETED_BLOG_HINTS_KEY = "deleted_blogs";
    
    // 新增：博客基本信息key前缀
    public static final String BLOG_INFO_KEY = "blog:info:";
    
    // 新增：用户已读博客集合key前缀
    public static final String USER_READ_BLOGS_KEY = "user:read:";
    
    // 新增：作者博客集合key前缀
    public static final String AUTHOR_BLOGS_KEY = "author:blogs:";
    
    // 新增：用户对特定作者的未读博客key前缀
    public static final String USER_UNREAD_BLOGS_KEY = "user:unread:";
    
    // 新增：用户未读计数key前缀（Hash结构）
    public static final String USER_UNREAD_COUNT_KEY = "user:unread:count:";
    
    // 新增：博客信息缓存过期时间，单位为天
    public static final Long BLOG_INFO_TTL = 7L;
    
    // 新增：用户已读博客过期时间，单位为天
    public static final Long USER_READ_TTL = 30L;
    
    // 新增：用户未读博客过期时间，单位为天
    public static final Long USER_UNREAD_TTL = 30L;
    
    // 商店评论列表缓存key前缀
    public static final String CACHE_SHOP_COMMENTS_KEY = "cache:shop:comments:";
    // 商店评论列表缓存过期时间，单位为分钟
    public static final Long CACHE_SHOP_COMMENTS_TTL = 10L;
    // 商店评分缓存key前缀
    public static final String CACHE_SHOP_SCORE_KEY = "cache:shop:score:";
    // 商店评分缓存过期时间，单位为分钟
    public static final Long CACHE_SHOP_SCORE_TTL = 30L;
    // 商店热门评论缓存key前缀
    public static final String CACHE_SHOP_HOT_COMMENTS_KEY = "cache:shop:hot_comments:";
    // 商店热门评论缓存过期时间，单位为小时
    public static final Long CACHE_SHOP_HOT_COMMENTS_TTL = 1L;
    
    // 商店评论缓存锁key前缀
    public static final String LOCK_SHOP_COMMENTS_KEY = "lock:shop:comments:";
    // 商店评论缓存锁过期时间，单位为秒
    public static final Long LOCK_SHOP_COMMENTS_TTL = 10L;
}
