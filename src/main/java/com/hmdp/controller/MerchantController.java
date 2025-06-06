package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.MerchantLoginFormDTO;
import com.hmdp.entity.Merchant;
import com.hmdp.entity.Shop;
import com.hmdp.service.IMerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 商家控制器
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Slf4j
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Resource
    private IMerchantService merchantService;

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return Result
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {
        return merchantService.sendCode(phone);
    }

    /**
     * 商家登录
     * @param loginForm 登录表单
     * @return Result
     */
    @PostMapping("/login")
    public Result login(@RequestBody MerchantLoginFormDTO loginForm) {
        return merchantService.login(loginForm);
    }

    /**
     * 商家退出登录
     * @param request 请求
     * @return Result
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return merchantService.logout(request);
    }

    /**
     * 获取商家信息
     * @return Result
     */
    @GetMapping("/info")
    public Result getMerchantInfo() {
        return merchantService.getMerchantInfo();
    }

    /**
     * 商家注册
     * @param merchant 商家信息
     * @param code 验证码
     * @return Result
     */
    @PostMapping("/register")
    public Result register(@RequestBody Merchant merchant, @RequestParam("code") String code) {
        return merchantService.register(merchant, code);
    }

    // ========== 店铺管理相关接口 ==========

    /**
     * 获取商家主要店铺信息（兼容现有前端）
     * @return Result
     */
    @GetMapping("/shop/info")
    public Result getMerchantShopInfo() {
        return merchantService.getMerchantShopInfo();
    }

    /**
     * 获取商家所有店铺列表
     * @return Result
     */
    @GetMapping("/shop/list")
    public Result getMerchantShops() {
        return merchantService.getMerchantShops();
    }

    /**
     * 根据ID获取商家指定店铺详情
     * @param shopId 店铺ID
     * @return Result
     */
    @GetMapping("/shop/{shopId}")
    public Result getMerchantShopById(@PathVariable("shopId") Long shopId) {
        return merchantService.getMerchantShopById(shopId);
    }

    /**
     * 创建商家店铺
     * @param shop 店铺信息
     * @return Result
     */
    @PostMapping("/shop")
    public Result createMerchantShop(@RequestBody Shop shop) {
        return merchantService.createMerchantShop(shop);
    }

    /**
     * 更新商家店铺信息
     * @param shopId 店铺ID
     * @param shop 店铺信息
     * @return Result
     */
    @PutMapping("/shop/{shopId}")
    public Result updateMerchantShop(@PathVariable("shopId") Long shopId, @RequestBody Shop shop) {
        return merchantService.updateMerchantShop(shopId, shop);
    }

    /**
     * 删除商家店铺
     * @param shopId 店铺ID
     * @return Result
     */
    @DeleteMapping("/shop/{shopId}")
    public Result deleteMerchantShop(@PathVariable("shopId") Long shopId) {
        return merchantService.deleteMerchantShop(shopId);
    }
}