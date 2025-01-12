package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.hmdp.common.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @Override
    public Result queryById(Long id) {
        //1.从Redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否存在缓存，如果存在，直接返回缓存数据
        if (StrUtil.isNotBlank(shopJson)) {
            // 使用Fastjson将JSON字符串反序列化为User对象
            Shop shop = JSON.parseObject(shopJson, Shop.class);
            return Result.success(shop);
        }
        //3.如果不存在缓存，根据id从数据库查询
        Shop shop = getById(id);
        //4.不存在，返回错误信息
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        //5.存在，将数据缓存到Redis
        // 使用Fastjson手动序列化User对象为JSON字符串
        String userJson = JSON.toJSONString(shop);
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, userJson,CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //6.返回商铺详情数据
        return Result.success(shop);
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
        if (id != null) {
            return Result.fail("店铺不存在");
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
