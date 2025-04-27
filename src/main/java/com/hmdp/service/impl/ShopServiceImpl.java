package com.hmdp.service.impl;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hmdp.common.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    /**
     * 根据id查询商铺信息
     *
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @Override
    public Result queryById(Long id) {
        //缓存穿透问题
        //Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //互斥锁解决缓存击穿问题
        //Shop shop = queryWithMutex(id);

        //用逻辑过期时间来解决缓存击穿问题
        //Shop shop = queryWithLogicalExpire(id);
        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        //6.返回商铺详情数据
        return Result.success(shop);
    }

    /**
     * @description: 根据id查询商铺信息，使用缓存空对象来解决缓存穿透问题
     * @author: yate
     * @date: 2025/1/14 0014 18:45
     * @param: [id]
     * @return: com.hmdp.entity.Shop
     **/
    public Shop queryWithPassThrough(Long id) {
        //1.从Redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断缓存是否命中
        if (shopJson != null) {
            // 判断缓存是否为空字符串，如果是，则返回错误信息,避免缓存穿透，否则返回缓存数据
            if (shopJson.equals("")) {
                //缓存为空字符串，返回错误信息
                return null;
            } else {
                //缓存非空字符串，使用Fastjson将JSON字符串反序列化为Shop对象
                Shop shop = JSON.parseObject(shopJson, Shop.class);
                return shop;
            }
        }
        //3.如果缓存中不存在数据或者空字符串，则根据id从数据库查询
        Shop shop = getById(id);
        //4.不存在，将空字符串缓存到Redis，返回错误信息
        if (shop == null) {
            //将空字符串缓存到Redis，避免频繁访问数据库
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            //返回错误信息
            return null;
        }
        //5.存在，将数据缓存到Redis
        // 使用Fastjson手动序列化User对象为JSON字符串
        String userJson = JSON.toJSONString(shop);
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, userJson, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //6.返回商铺详情数据
        return shop;
    }

    /**
     * @description: 根据id查询商铺信息，使用互斥锁解决缓存击穿问题+缓存空对象来解决缓存击穿问题
     * @author: yate
     * @date: 2025/1/14 0014 18:47
     * @param: [id]
     * @return: com.hmdp.entity.Shop
     **/
    public Shop queryWithMutex(Long id) {
        //1.从Redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断缓存是否命中
        if (shopJson != null) {
            // 判断缓存是否为空字符串，如果是，则返回错误信息,避免缓存穿透，否则返回缓存数据
            if (shopJson.equals("")) {
                //缓存为空字符串，返回错误信息
                return null;
            } else {
                //缓存非空字符串，使用Fastjson将JSON字符串反序列化为Shop对象
                Shop shop = JSON.parseObject(shopJson, Shop.class);
                return shop;
            }
        }
        //3.如果缓存中不存在数据,实现缓存重建机制，使用互斥锁
        //3.1尝试获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean islock = trylock(lockKey);
            //3.2判断是否获取到锁
            if (!islock) {
                //3.3获取失败，则休眠并重试
                Thread.sleep(50);
                //3.4重新调用查询方法,查询缓存
                return queryWithMutex(id);
            }

            //3.5获取成功，则根据id从数据库查询
            shop = getById(id);
            //模拟重建的延迟
            Thread.sleep(200);
            //4.不存在，将空字符串缓存到Redis，返回错误信息
            if (shop == null) {
                //将空字符串缓存到Redis，避免频繁访问数据库
                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                //返回错误信息
                return null;
            }
            //5.存在，将数据缓存到Redis
            // 使用Fastjson手动序列化User对象为JSON字符串
            String userJson = JSON.toJSONString(shop);
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, userJson, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //6.释放互斥锁
            unlock(lockKey);
        }
        //7.返回商铺详情数据
        return shop;
    }

    /**
     * @description: 缓存预热，将数据库中所有商铺信息和过期时间缓存到Redis中
     * @author: yate
     * @date: 2025/1/14 0014 20:49
     * @param: [id, expireSeconds]
     * @return: void
     **/
    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        //1.查询店铺数据
        Shop shop = getById(id);
        Thread.sleep(200);
        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.写入Redis缓存
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSON.toJSONString(redisData));
    }

    /**
     * @description: 根据id查询商铺信息，使用逻辑过期解决缓存击穿问题+缓存空对象来解决缓存击穿问题
     * @author: yate
     * @date: 2025/1/14 0014 20:47
     * @param: [id]
     * @return: com.hmdp.entity.Shop
     **/

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public Shop queryWithLogicalExpire(Long id) {
        //1.从Redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断缓存是否命中
        //如果未命中
        if (StrUtil.isBlank(shopJson)) {
            return null;
        }
        //3.如果命中，需要先将JSON字符串反序列化为RedisData对象
        RedisData redisData = JSON.parseObject(shopJson, RedisData.class);
        //获取到的数据转换为JSONObject对象，再转换为Shop对象
        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSON.toJavaObject(data, Shop.class);
        //4.判断缓存是否过期
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            //4.1如果未过期，则直接返回商铺信息
            return shop;
        }
        //4.2如果已过期，则需要缓存重建
        //5.缓存重建
        //5.1尝试获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean islock = trylock(lockKey);
        //5.2判断是否获取到锁
        if (!islock) {
            //5.3获取失败，则返回过期的商铺数据
            return shop;
        }
        //5.4获取成功，则开启独立线程，实现缓存重建
        CACHE_REBUILD_EXECUTOR.submit(() -> {
            try {
                this.saveShop2Redis(id, 20L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                //6.释放互斥锁
                unlock(lockKey);
            }
        });
        //5.5缓存重建过程中，返回过期的商铺数据
        /*在获取成功锁后，应该返回过期的商铺数据 shop。
        原因如下：
        在缓存已过期且成功获取到锁的情况下，虽然会开启独立线程进行缓存重建，
        但当前请求线程不能等待缓存重建完成后再返回结果，因为这样会阻塞当前请求，影响用户体验。
        而返回过期的商铺数据，虽然数据不是最新的，但至少能保证用户在等待缓存重建期间仍然可以获取到商铺信息，
        避免因缓存过期导致用户完全无法获取数据的情况。同时，后续缓存重建完成后，
        新的数据会被更新到缓存中，后续请求就能获取到最新的数据了。*/
        return shop;
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


    /**
     * @description: 更新商铺信息
     * @author: yate
     * @date: 2025/1/11 0011 20:16
     * @param: [shop]
     * @return: com.hmdp.common.Result
     **/
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();

        if (id == null) {
            return Result.fail("商铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        log.info("更新商铺信息成功，id={}", id);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        log.info("删除缓存成功，id={}", id);
        // 3.返回成功信息
        return Result.success("更新成功");
    }
}
