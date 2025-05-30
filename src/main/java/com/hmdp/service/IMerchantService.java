package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.dto.MerchantLoginFormDTO;
import com.hmdp.entity.Merchant;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 商家服务接口
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
public interface IMerchantService extends IService<Merchant> {

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return Result
     */
    Result sendCode(String phone);

    /**
     * 商家登录
     * @param loginForm 登录表单
     * @return Result
     */
    Result login(MerchantLoginFormDTO loginForm);

    /**
     * 商家退出登录
     * @param request 请求
     * @return Result
     */
    Result logout(HttpServletRequest request);

    /**
     * 获取商家信息
     * @return Result
     */
    Result getMerchantInfo();

    /**
     * 商家注册
     * @param merchant 商家信息
     * @param code 验证码
     * @return Result
     */
    Result register(Merchant merchant, String code);
} 