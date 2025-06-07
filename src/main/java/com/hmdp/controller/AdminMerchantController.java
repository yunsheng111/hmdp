package com.hmdp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.Result;
import com.hmdp.entity.Merchant;
import com.hmdp.entity.MerchantQualification;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.MerchantQualificationMapper;
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
 * 管理员商户管理控制器
 * </p>
 *
 * @author yate
 * @since 2024-07-31
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Api(tags = "管理员商户管理接口")
public class AdminMerchantController {

    @Resource
    private IMerchantService merchantService;

    @Resource
    private IShopService shopService;

    @Resource
    private IShopTypeService shopTypeService;
    
    @Resource
    private MerchantQualificationMapper merchantQualificationMapper;

    /**
     * 获取商户列表
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param status 状态
     * @param shopTypeId 商户分类ID
     * @return 商户列表
     */
    @GetMapping("/merchants")
    @ApiOperation("获取商户列表")
    public Result getMerchants(
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @ApiParam("关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @ApiParam("状态：0-待审核，1-正常，2-禁用，3-待整改") @RequestParam(value = "status", required = false) Integer status,
            @ApiParam("商户分类ID") @RequestParam(value = "shopTypeId", required = false) Long shopTypeId) {
        
        log.info("管理员获取商户列表，页码：{}，每页大小：{}，关键词：{}，状态：{}，分类ID：{}", 
                page, size, keyword, status, shopTypeId);
        
        try {
            // 创建查询条件
            LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();

            // 关键词过滤（商户名称、电话）
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(Merchant::getName, keyword)
                        .or()
                        .like(Merchant::getPhone, keyword);
            }

            // 状态过滤
            if (status != null) {
                queryWrapper.eq(Merchant::getStatus, status);
            }

            // 商户分类过滤
            if (shopTypeId != null) {
                queryWrapper.eq(Merchant::getTypeId, shopTypeId);
            }

            // 创建分页对象
            Page<Merchant> merchantPage = new Page<>(page, size);

            // 执行分页查询
            Page<Merchant> result = merchantService.page(merchantPage, queryWrapper);

            // 获取所有商户分类
            List<ShopType> shopTypes = shopTypeService.list();
            Map<Long, String> shopTypeMap = new HashMap<>();
            for (ShopType shopType : shopTypes) {
                shopTypeMap.put(shopType.getId(), shopType.getName());
            }

            // 为商户列表设置分类名称
            List<Merchant> merchants = result.getRecords();
            for (Merchant merchant : merchants) {
                if (merchant.getTypeId() != null) {
                    merchant.setTypeName(shopTypeMap.get(merchant.getTypeId()));
                } else {
                    merchant.setTypeName("未分类");
                }
            }

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("list", merchants);
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            data.put("size", result.getSize());
            data.put("shopTypes", shopTypeMap);

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取商户列表失败", e);
            return Result.fail("获取商户列表失败");
        }
    }

    /**
     * 获取商户详情
     * @param merchantId 商户ID
     * @return 商户详情信息
     */
    @GetMapping("/merchants/{merchantId}")
    @ApiOperation("获取商户详情")
    public Result getMerchantDetail(@PathVariable("merchantId") Long merchantId) {
        log.info("管理员获取商户详情，商户ID：{}", merchantId);

        try {
            // 查询商户信息
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null) {
                return Result.fail("商户不存在");
            }

            // 获取商户分类信息
            if (merchant.getTypeId() != null) {
                ShopType shopType = shopTypeService.getById(merchant.getTypeId());
                if (shopType != null) {
                    merchant.setTypeName(shopType.getName());
                }
            } else {
                merchant.setTypeName("未分类");
            }

            return Result.success(merchant);
        } catch (Exception e) {
            log.error("获取商户详情失败", e);
            return Result.fail("获取商户详情失败");
        }
    }

    /**
     * 编辑商户信息
     * @param merchantId 商户ID
     * @param merchantData 商户信息
     * @return 更新结果
     */
    @PutMapping("/merchants/{merchantId}")
    @ApiOperation("编辑商户信息")
    public Result updateMerchant(
            @PathVariable("merchantId") Long merchantId,
            @RequestBody Map<String, Object> merchantData) {

        log.info("管理员编辑商户信息，商户ID：{}，商户信息：{}", merchantId, merchantData);

        try {
            // 查询商户是否存在
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null) {
                return Result.fail("商户不存在");
            }

            // 更新商户信息
            if (merchantData.containsKey("name")) {
                merchant.setName((String) merchantData.get("name"));
            }
            if (merchantData.containsKey("phone")) {
                merchant.setPhone((String) merchantData.get("phone"));
            }
            if (merchantData.containsKey("avatar")) {
                merchant.setAvatar((String) merchantData.get("avatar"));
            }
            if (merchantData.containsKey("description")) {
                merchant.setDescription((String) merchantData.get("description"));
            }
            if (merchantData.containsKey("typeId")) {
                Object typeIdObj = merchantData.get("typeId");
                if (typeIdObj != null) {
                    merchant.setTypeId(Long.valueOf(typeIdObj.toString()));
                } else {
                    merchant.setTypeId(null);
                }
            }

            merchant.setUpdateTime(LocalDateTime.now());

            boolean updated = merchantService.updateById(merchant);

            if (!updated) {
                return Result.fail("更新商户信息失败");
            }

            return Result.success("更新商户信息成功");
        } catch (Exception e) {
            log.error("编辑商户信息失败", e);
            return Result.fail("编辑商户信息失败");
        }
    }

    /**
     * 获取商户资质信息
     * @param merchantId 商户ID
     * @return 商户资质信息
     */
    @GetMapping("/merchants/{merchantId}/qualifications")
    @ApiOperation("获取商户资质信息")
    public Result getMerchantQualifications(@PathVariable("merchantId") Long merchantId) {
        log.info("管理员获取商户资质信息，商户ID：{}", merchantId);
        
        try {
            // 查询商户是否存在
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null) {
                return Result.fail("商户不存在");
            }
            
            // 查询商户资质信息
            LambdaQueryWrapper<MerchantQualification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MerchantQualification::getMerchantId, merchantId);
            MerchantQualification qualification = merchantQualificationMapper.selectOne(queryWrapper);
            
            if (qualification == null) {
                return Result.fail("商户资质信息不存在");
            }
            
            return Result.success(qualification);
        } catch (Exception e) {
            log.error("获取商户资质信息失败", e);
            return Result.fail("获取商户资质信息失败");
        }
    }
    
    /**
     * 审核商户申请
     * @param merchantId 商户ID
     * @param auditForm 审核表单
     * @return 审核结果
     */
    @PutMapping("/merchants/{merchantId}/audit")
    @ApiOperation("审核商户申请")
    public Result auditMerchantApplication(
            @PathVariable("merchantId") Long merchantId,
            @RequestBody Map<String, Object> auditForm) {
        
        log.info("管理员审核商户申请，商户ID：{}，审核结果：{}", merchantId, auditForm);
        
        try {
            // 获取审核结果和意见
            Integer status = (Integer) auditForm.get("status");
            String auditComment = (String) auditForm.get("auditComment");
            
            if (status == null) {
                return Result.fail("审核结果不能为空");
            }
            
            // 如果是拒绝，必须有审核意见
            if (status == 2 && !StringUtils.hasText(auditComment)) {
                return Result.fail("拒绝时必须填写审核意见");
            }
            
            // 查询商户是否存在
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null) {
                return Result.fail("商户不存在");
            }
            
            // 更新商户状态
            merchant.setStatus(status);
            if (status == 2) {
                merchant.setRejectReason(auditComment);
            }
            merchant.setUpdateTime(LocalDateTime.now());
            
            boolean updated = merchantService.updateById(merchant);
            
            if (!updated) {
                return Result.fail("审核失败");
            }
            
            return Result.success("审核成功");
        } catch (Exception e) {
            log.error("审核商户申请失败", e);
            return Result.fail("审核商户申请失败");
        }
    }
    
    /**
     * 更新商户状态
     * @param merchantId 商户ID
     * @param statusMap 状态信息
     * @return 更新结果
     */
    @PutMapping("/merchants/{merchantId}/status")
    @ApiOperation("更新商户状态")
    public Result updateMerchantStatus(
            @PathVariable("merchantId") Long merchantId,
            @RequestBody Map<String, Object> statusMap) {
        
        log.info("管理员更新商户状态，商户ID：{}，状态信息：{}", merchantId, statusMap);
        
        try {
            // 获取状态和原因
            Integer status = (Integer) statusMap.get("status");
            String reason = (String) statusMap.get("reason");
            
            if (status == null) {
                return Result.fail("状态不能为空");
            }
            
            // 如果是禁用，必须有原因
            if (status == 2 && !StringUtils.hasText(reason)) {
                return Result.fail("禁用商户时必须填写原因");
            }
            
            // 查询商户是否存在
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant == null) {
                return Result.fail("商户不存在");
            }
            
            // 更新商户状态
            merchant.setStatus(status);
            if (status == 2) {
                merchant.setRejectReason(reason);
            }
            merchant.setUpdateTime(LocalDateTime.now());
            
            boolean updated = merchantService.updateById(merchant);
            
            if (!updated) {
                return Result.fail("更新商户状态失败");
            }
            
            return Result.success("更新商户状态成功");
        } catch (Exception e) {
            log.error("更新商户状态失败", e);
            return Result.fail("更新商户状态失败");
        }
    }
} 