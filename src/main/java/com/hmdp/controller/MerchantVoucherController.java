package com.hmdp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hmdp.common.Result;
import com.hmdp.dto.*;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.MerchantHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 优惠券管理 - 商家端接口
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@RestController
@RequestMapping("/merchant/voucher")
@Api(tags = "商家端-优惠券管理接口")
public class MerchantVoucherController {

    @Resource
    private IVoucherService voucherService;

    /**
     * 创建普通优惠券
     */
    @PostMapping
    @ApiOperation("创建普通优惠券")
    public Result createVoucher(@Valid @RequestBody VoucherCreateDTO createDTO) {
        // 设置当前商家的店铺ID
        Long shopId = MerchantHolder.getMerchant().getShopId();
        createDTO.setShopId(shopId);
        
        VoucherDetailDTO result = voucherService.createVoucher(createDTO);
        return Result.success(result);
    }

    /**
     * 创建秒杀优惠券
     */
    @PostMapping("/seckill")
    @ApiOperation("创建秒杀优惠券")
    public Result createSeckillVoucher(@Valid @RequestBody SeckillVoucherCreateDTO createDTO) {
        // 设置当前商家的店铺ID
        Long shopId = MerchantHolder.getMerchant().getShopId();
        createDTO.setShopId(shopId);
        
        VoucherDetailDTO result = voucherService.createSeckillVoucher(createDTO);
        return Result.success(result);
    }

    /**
     * 获取优惠券列表
     */
    @GetMapping("/list")
    @ApiOperation("获取优惠券列表")
    public Result getVoucherList(@Valid VoucherQueryDTO queryDTO) {
        // 设置当前商家的店铺ID
        Long shopId = MerchantHolder.getMerchant().getShopId();
        queryDTO.setShopId(shopId);
        
        IPage<VoucherDetailDTO> result = voucherService.queryVoucherList(queryDTO);
        return Result.success(result.getRecords(), result.getTotal());
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取优惠券详情")
    public Result getVoucherDetail(@PathVariable Long id) {
        Long shopId = MerchantHolder.getMerchant().getShopId();
        VoucherDetailDTO result = voucherService.getVoucherDetail(id, shopId);
        return Result.success(result);
    }

    /**
     * 更新优惠券状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新优惠券状态")
    public Result updateVoucherStatus(@PathVariable Long id, 
                                    @Valid @RequestBody VoucherStatusUpdateDTO updateDTO) {
        Long shopId = MerchantHolder.getMerchant().getShopId();
        voucherService.updateVoucherStatus(id, shopId, updateDTO.getStatus());
        return Result.success();
    }

    /**
     * 更新优惠券库存
     */
    @PutMapping("/{id}/stock")
    @ApiOperation("更新优惠券库存")
    public Result updateVoucherStock(@PathVariable Long id, 
                                   @Valid @RequestBody VoucherStockUpdateDTO updateDTO) {
        Long shopId = MerchantHolder.getMerchant().getShopId();
        VoucherStockUpdateResultDTO result = voucherService.updateVoucherStock(id, shopId, updateDTO);
        return Result.success(result);
    }
    
    /**
     * 更新普通优惠券信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新普通优惠券信息")
    public Result updateVoucher(@PathVariable Long id, 
                              @Valid @RequestBody VoucherUpdateDTO updateDTO) {
        Long shopId = MerchantHolder.getMerchant().getShopId();
        VoucherDetailDTO result = voucherService.updateVoucher(id, shopId, updateDTO);
        return Result.success(result);
    }
    
    /**
     * 更新秒杀优惠券信息
     */
    @PutMapping("/seckill/{id}")
    @ApiOperation("更新秒杀优惠券信息")
    public Result updateSeckillVoucher(@PathVariable Long id, 
                                     @Valid @RequestBody SeckillVoucherUpdateDTO updateDTO) {
        Long shopId = MerchantHolder.getMerchant().getShopId();
        VoucherDetailDTO result = voucherService.updateSeckillVoucher(id, shopId, updateDTO);
        return Result.success(result);
    }
}
