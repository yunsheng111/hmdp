package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * @ Tool：IntelliJ IDEA
 * @ Author：云生
 * @ Date：2025-01-14-20:35
 * @ Version：1.0
 * @ Description：
 */
@SpringBootTest
@Slf4j
public class TestShopServiceImpl {
    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private CacheClient cacheClient;

    @Test
    void testSaveShop2Redis() throws InterruptedException {
        shopService.saveShop2Redis(1L,10L);
        log.info("商铺1数据已成功缓存到Redis");
    }

    @Test
    void testSaveShop() {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + 1L,shop,10L, TimeUnit.SECONDS);
    }
    
    @Test
    void testSaveShop4() {
        Shop shop = shopService.getById(4L);
        if (shop != null) {
            cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + 4L, shop, 10L, TimeUnit.SECONDS);
            System.out.println("商铺4数据已成功缓存到Redis");
        } else {
            System.out.println("商铺4在数据库中不存在");
        }
    }

    /*
     * 缓存所有商铺信息
     */
    @Test
    void testSaveAllShop() {
        List<Shop> shops = shopService.list();
        for (Shop shop : shops) {
            cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + shop.getId(), shop, 10L, TimeUnit.SECONDS);
        }
    }
}

