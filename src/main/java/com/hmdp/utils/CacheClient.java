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
            log.debug("缓存空值，key={}, TTL={}分钟", key, CACHE_NULL_TTL);
            //返回错误信息
            return null;
        }
        //5.存在，将数据缓存到Redis
        // 使用Fastjson手动序列化对象为JSON字符串
        this.set(key, r, time, unit);
        log.debug("缓存数据，key={}, TTL={}分钟", key, time);
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
        log.debug("开始查询缓存，key={}", key);
        //1.从Redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        //如果未命中
        if (StrUtil.isBlank(json)) {
            log.debug("缓存未命中，key={}", key);
            // 缓存未命中，从数据库查询
            R r = dbFallback.apply(id);
            // 如果数据库中也不存在
            if (r == null) {
                log.debug("数据库中也不存在，key={}", key);
                // 将空值写入缓存，避免缓存穿透
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                log.debug("缓存空值，key={}, TTL={}分钟", key, CACHE_NULL_TTL);
                return null;
            }
            // 数据库中存在，写入缓存并设置逻辑过期时间
            this.setWithLogicalExpire(key, r, time, unit);
            log.debug("缓存数据(逻辑过期)，key={}, TTL={}秒", key, unit.toSeconds(time));
            return r;
        }
        
        log.debug("缓存命中，key={}, json={}", key, json);
        //3.如果命中，需要先将JSON字符串反序列化为RedisData对象
        RedisData redisData = JSON.parseObject(json, RedisData.class);
        //获取到的数据转换为JSONObject对象，再转换为Shop对象
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSON.toJavaObject(data, type);
        
        // 检查反序列化后的对象是否正确
        if (r == null) {
            log.error("缓存数据反序列化失败，key={}, json={}", key, json);
            // 从数据库重新获取
            r = dbFallback.apply(id);
            if (r != null) {
                // 更新缓存
                this.setWithLogicalExpire(key, r, time, unit);
                log.debug("更新缓存数据，key={}", key);
            }
            return r;
        }
        
        //4.判断缓存是否过期
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            //4.1如果未过期，则直接返回缓存数据信息
            log.debug("缓存未过期，key={}", key);
            return r;
        }
        
        log.debug("缓存已过期，key={}", key);
        //4.2如果已过期，则需要缓存重建
        //5.缓存重建
        //5.1尝试获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean islock = trylock(lockKey);
        //5.2判断是否获取到锁
        if (!islock) {
            //5.3获取失败，则返回过期的商铺数据
            log.debug("获取锁失败，返回过期数据，key={}", key);
            return r;
        }
        
        log.debug("获取锁成功，开始异步重建缓存，key={}", key);
        //5.4获取成功，则开启独立线程，实现缓存重建
        CACHE_REBUILD_EXECUTOR.submit(() -> {
            try {
                //查询数据库，获取最新数据
                R r1 = dbFallback.apply(id);
                //写入缓存
                this.setWithLogicalExpire(key,r1,time,unit);
                log.debug("异步重建缓存完成，key={}", key);
            } catch (Exception e) {
                log.error("异步重建缓存失败，key={}", key, e);
                throw new RuntimeException(e);
            } finally {
                //6.释放互斥锁
                unlock(lockKey);
                log.debug("释放锁，key={}", lockKey);
            }
        });
        //7.返回缓存中的过期数据
        return r;
    }

    /**
     * @description: 尝试获取分布式锁，增加重试机制
     * @author: yate
     * @date: 2025/1/13 0013 20:30
     * @param: [key]
     * @return: boolean
     **/
    private boolean trylock(String key) {
        // 尝试获取锁，最多重试3次
        for (int i = 0; i < 3; i++) {
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
            //因为`setIfAbsent`方法的返回值是一个包装类`Boolean`，
            // 而不是基本数据类型`boolean`，所以需要使用BooleanUtil工具类进行拆箱，不然会出现空指针异常
            if (BooleanUtil.isTrue(flag)) {
                log.debug("获取锁成功，key={}", key);
                return true;
            }

            try {
                // 获取锁失败，等待100ms后重试
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("获取锁等待被中断", e);
                Thread.currentThread().interrupt();
            }
        }
        log.debug("获取锁失败，key={}", key);
        return false;
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
