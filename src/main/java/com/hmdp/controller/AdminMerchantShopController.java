package com.hmdp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.Result;
import com.hmdp.entity.Merchant;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IMerchantService;
import com.hmdp.service.IShopService;
import com.hmdp.service.IShopTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员商户店铺管理控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Api(tags = "管理员商户店铺管理接口")
public class AdminMerchantShopController {

    @Resource
    private IShopService shopService;

    @Resource
    private IMerchantService merchantService;

    @Resource
    private IShopTypeService shopTypeService;

    /**
     * 获取所有商户店铺列表
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词（店铺名称）
     * @param merchantId 商户ID
     * @param status 店铺状态
     * @param typeId 店铺分类ID
     * @return 店铺列表
     */
    @GetMapping("/merchant-shops")
    @ApiOperation("获取所有商户店铺列表")
    public Result getMerchantShops(
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @ApiParam("关键词（店铺名称）") @RequestParam(value = "keyword", required = false) String keyword,
            @ApiParam("商户ID") @RequestParam(value = "merchantId", required = false) Long merchantId,
            @ApiParam("店铺状态：0-休息，1-营业") @RequestParam(value = "status", required = false) Integer status,
            @ApiParam("店铺分类ID") @RequestParam(value = "typeId", required = false) Long typeId) {
        
        log.info("管理员获取商户店铺列表，页码：{}，每页大小：{}，关键词：{}，商户ID：{}，状态：{}，分类ID：{}", 
                page, size, keyword, merchantId, status, typeId);
        
        try {
            // 创建查询条件
            LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();

            // 关键词过滤（店铺名称）
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(Shop::getName, keyword);
            }

            // 商户ID过滤
            if (merchantId != null) {
                queryWrapper.eq(Shop::getMerchantId, merchantId);
            }

            // 状态过滤
            if (status != null) {
                queryWrapper.eq(Shop::getStatus, status);
            }

            // 店铺分类过滤
            if (typeId != null) {
                queryWrapper.eq(Shop::getTypeId, typeId);
            }

            // 按创建时间倒序排列
            queryWrapper.orderByDesc(Shop::getCreateTime);

            // 创建分页对象
            Page<Shop> shopPage = new Page<>(page, size);

            // 执行分页查询
            Page<Shop> result = shopService.page(shopPage, queryWrapper);

            // 获取所有商户信息，用于显示商户名称
            List<Merchant> merchants = merchantService.list();
            Map<Long, String> merchantMap = new HashMap<>();
            for (Merchant merchant : merchants) {
                merchantMap.put(merchant.getId(), merchant.getName());
            }

            // 获取所有店铺分类信息
            List<ShopType> shopTypes = shopTypeService.list();
            Map<Long, String> shopTypeMap = new HashMap<>();
            for (ShopType shopType : shopTypes) {
                shopTypeMap.put(shopType.getId(), shopType.getName());
            }

            // 为每个店铺添加商户名称和分类名称
            for (Shop shop : result.getRecords()) {
                // 设置商户名称
                String merchantName = merchantMap.get(shop.getMerchantId());
                shop.setMerchantName(merchantName != null ? merchantName : "未知商户");
                
                // 设置分类名称
                String typeName = shopTypeMap.get(shop.getTypeId());
                shop.setTypeName(typeName != null ? typeName : "未分类");
            }

            // 构建返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            data.put("size", result.getSize());

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取商户店铺列表失败", e);
            return Result.fail("获取商户店铺列表失败");
        }
    }

    /**
     * 获取指定店铺详情
     * @param shopId 店铺ID
     * @return 店铺详情信息
     */
    @GetMapping("/merchant-shops/{shopId}")
    @ApiOperation("获取指定店铺详情")
    public Result getShopDetail(@PathVariable("shopId") Long shopId) {
        log.info("管理员获取店铺详情，店铺ID：{}", shopId);

        try {
            // 查询店铺信息
            Shop shop = shopService.getById(shopId);
            if (shop == null) {
                return Result.fail("店铺不存在");
            }

            // 获取商户信息
            if (shop.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(shop.getMerchantId());
                if (merchant != null) {
                    shop.setMerchantName(merchant.getName());
                }
            }

            // 获取店铺分类信息
            if (shop.getTypeId() != null) {
                ShopType shopType = shopTypeService.getById(shop.getTypeId());
                if (shopType != null) {
                    shop.setTypeName(shopType.getName());
                }
            }

            return Result.success(shop);
        } catch (Exception e) {
            log.error("获取店铺详情失败", e);
            return Result.fail("获取店铺详情失败");
        }
    }

    /**
     * 获取指定商户的所有店铺
     * @param merchantId 商户ID
     * @return 商户的店铺列表
     */
    @GetMapping("/merchants/{merchantId}/shops")
    @ApiOperation("获取指定商户的所有店铺")
    public Result getMerchantShops(@PathVariable("merchantId") Long merchantId) {
        log.info("管理员获取指定商户的店铺列表，商户ID：{}", merchantId);

        try {
            // 验证商户是否存在
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null) {
                return Result.fail("商户不存在");
            }

            // 查询该商户的所有店铺
            LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Shop::getMerchantId, merchantId)
                       .orderByDesc(Shop::getCreateTime);

            List<Shop> shops = shopService.list(queryWrapper);

            // 获取店铺分类信息
            List<ShopType> shopTypes = shopTypeService.list();
            Map<Long, String> shopTypeMap = new HashMap<>();
            for (ShopType shopType : shopTypes) {
                shopTypeMap.put(shopType.getId(), shopType.getName());
            }

            // 为每个店铺添加分类名称和商户名称
            for (Shop shop : shops) {
                shop.setMerchantName(merchant.getName());
                String typeName = shopTypeMap.get(shop.getTypeId());
                shop.setTypeName(typeName != null ? typeName : "未分类");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("merchantInfo", merchant);
            data.put("shops", shops);
            data.put("total", shops.size());

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取指定商户的店铺列表失败", e);
            return Result.fail("获取指定商户的店铺列表失败");
        }
    }

    /**
     * 修改店铺状态
     * @param shopId 店铺ID
     * @param statusMap 状态信息
     * @return 操作结果
     */
    @PutMapping("/merchant-shops/{shopId}/status")
    @ApiOperation("修改店铺状态")
    public Result updateShopStatus(
            @PathVariable("shopId") Long shopId,
            @RequestBody Map<String, Object> statusMap) {
        
        log.info("管理员修改店铺状态，店铺ID：{}，状态信息：{}", shopId, statusMap);
        
        try {
            // 获取状态和原因
            Integer status = (Integer) statusMap.get("status");
            String reason = (String) statusMap.get("reason");
            
            if (status == null) {
                return Result.fail("状态不能为空");
            }
            
            // 验证状态值是否有效
            if (status != 0 && status != 1) {
                return Result.fail("无效的状态值，只能是0（休息）或1（营业）");
            }
            
            // 查询店铺是否存在
            Shop shop = shopService.getById(shopId);
            if (shop == null) {
                return Result.fail("店铺不存在");
            }
            
            // 更新店铺状态
            shop.setStatus(status);
            shop.setUpdateTime(LocalDateTime.now());
            
            boolean updated = shopService.updateById(shop);
            
            if (!updated) {
                return Result.fail("更新店铺状态失败");
            }
            
            // 返回成功结果
            String statusText = status == 0 ? "休息" : "营业";
            return Result.success("店铺状态已更新为" + statusText);
        } catch (Exception e) {
            log.error("修改店铺状态失败，店铺ID：{}", shopId, e);
            return Result.fail("修改店铺状态失败");
        }
    }

    /**
     * 更新店铺信息
     * @param shopId 店铺ID
     * @param shopForm 店铺信息
     * @return 操作结果
     */
    @PutMapping("/merchant-shops/{shopId}")
    @ApiOperation("更新店铺信息")
    public Result updateShop(
            @PathVariable("shopId") Long shopId,
            @RequestBody Shop shopForm) {

        log.info("管理员更新店铺信息，店铺ID：{}，店铺信息：{}", shopId, shopForm);

        try {
            // 查询店铺是否存在
            Shop shop = shopService.getById(shopId);
            if (shop == null) {
                return Result.fail("店铺不存在");
            }

            // 更新店铺信息
            shop.setName(shopForm.getName());
            shop.setTypeId(shopForm.getTypeId());
            shop.setImages(shopForm.getImages());
            shop.setArea(shopForm.getArea());
            shop.setAddress(shopForm.getAddress());
            shop.setX(shopForm.getX());
            shop.setY(shopForm.getY());
            shop.setAvgPrice(shopForm.getAvgPrice());
            shop.setOpenHours(shopForm.getOpenHours());
            shop.setUpdateTime(LocalDateTime.now());

            boolean updated = shopService.updateById(shop);

            if (!updated) {
                return Result.fail("更新店铺信息失败");
            }

            return Result.success("更新店铺信息成功");
        } catch (Exception e) {
            log.error("更新店铺信息失败，店铺ID：{}", shopId, e);
            return Result.fail("更新店铺信息失败");
        }
    }
}
