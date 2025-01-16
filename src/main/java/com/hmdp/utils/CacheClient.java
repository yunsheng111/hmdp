package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;

/**
 * @ Tool：IntelliJ IDEA
 * @ Author：云生
 * @ Date：2025-01-15-18:42
 * @ Version：1.0
 * @ Description：
 * 基于StringRedisTemplate封装一个缓存工具类，满足下列需求：
 * * 方法1：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
 * * 方法2：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
 * * 方法3：根据指定的key查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
 * * 方法4：根据指定的key查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
 */
@Component
@Slf4j
public class CacheClient {
    private StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate redisTemplate) {
        this.stringRedisTemplate = redisTemplate;
    }


    /**
     * @description: 方法1：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
     * TimeUnit：时间单位
     * @author: yate
     * @date: 2025/1/15 0015 18:53
     * @param: [key, value, time, unit]
     * @return: void
     **/
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value), time, unit);

    }
    /**
     * @description: 方法2：将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
     * RedisData：自定义的缓存数据结构，包含数据和过期时间
     * @author: yate
     * @date: 2025/1/15 0015 18:55
     * @param: [key, value, time, unit]
     * @return: void
     **/
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        //写入缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(redisData));
    }

    /**
     * @description: 方法3：根据指定的key查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
     *
     * @author: yate
     * @date: 2025/1/15 0015 19:25
     * @param: [KeyPrefix, id, type, dbFallback, time, unit]
     * @return: R
     **/
    public <R,ID> R queryWithPassThrough(String KeyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback, Long time, TimeUnit unit) {
        String key = KeyPrefix + id;
        //1.从Redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        if (json != null) {
            // 判断缓存是否为空字符串，如果是，则返回错误信息,避免缓存穿透，否则返回缓存数据
            if (json.equals("")) {
                //缓存为空字符串，返回错误信息
                return null;
            } else {
                //缓存非空字符串，使用Fastjson将JSON字符串反序列化为Shop对象
                return JSON.parseObject(json,type);

            }
        }
        //3.如果缓存中不存在数据或者空字符串，则根据id从数据库查询
        R r = dbFallback.apply(id);
        //4.不存在，将空字符串缓存到Redis，返回错误信息
        if (r == null) {
            //将空字符串缓存到Redis，避免频繁访问数据库
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            //返回错误信息
            return null;
        }
        //5.存在，将数据缓存到Redis
        // 使用Fastjson手动序列化User对象为JSON字符串
        this.set(key, r, time, unit);
        //6.返回商铺详情数据
        return r;
    }

    /**
     * @description: 方法4：根据指定的key查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
     * @author: yate
     * @date: 2025/1/15 0015 19:34
     * @param: [id]
     * @return: com.hmdp.entity.Shop
     **/
    //定义线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <R,ID> R queryWithLogicalExpire
            (String KeyPrefix, ID id,Class<R> type, Function<ID,R> dbFallback,
            Long time, TimeUnit unit) {

        String key = KeyPrefix + id;
        //1.从Redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        //如果未命中
        if (StrUtil.isBlank(json)) {
            return null;
        }
        //3.如果命中，需要先将JSON字符串反序列化为RedisData对象
        RedisData redisData = JSON.parseObject(json, RedisData.class);
        //获取到的数据转换为JSONObject对象，再转换为Shop对象
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSON.toJavaObject(data, type);
        //4.判断缓存是否过期
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            //4.1如果未过期，则直接返回缓存数据信息
            return r;
        }
        //4.2如果已过期，则需要缓存重建
        //5.缓存重建
        //5.1尝试获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean islock = trylock(lockKey);
        //5.2判断是否获取到锁
        if (!islock) {
            //5.3获取失败，则返回过期的商铺数据
            return r;
        }
        //5.4获取成功，则开启独立线程，实现缓存重建
        CACHE_REBUILD_EXECUTOR.submit(() -> {
            try {
                //查询数据库，获取最新数据
                R r1 = dbFallback.apply(id);
                //写入缓存
                this.setWithLogicalExpire(key,r1,time,unit);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                //6.释放互斥锁
                unlock(lockKey);
            }
        });
        //7.返回缓存中的过期数据
        return r;
    }

    /**
     * @description: 尝试获取分布式锁
     * @author: yate
     * @date: 2025/1/13 0013 20:30
     * @param: [key]
     * @return: boolean
     **/
    private boolean trylock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        //因为因为`setIfAbsent`方法的返回值是一个包装类`Boolean`，
        // 而不是基本数据类型`boolean`，所以需要使用BooleanUtil工具类进行拆箱，不然会出现空指针异常
        return BooleanUtil.isTrue(flag);
    }

    /**
     * @description: 释放分布式锁
     * @author: yate
     * @date: 2025/1/13 0013 20:38
     * @param: [key]
     * @return: void
     **/
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }


}
