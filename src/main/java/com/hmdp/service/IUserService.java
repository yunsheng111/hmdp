package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.Result;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    /**
     * 发送手机验证码
     *@param: phone  session
     *@return: Result
     *@Author: yate
     */

    Result sendCode(String phone, HttpSession session);

    /**
     * 登录
     *@param: loginForm session
     *@return:  Result
     *@Author: yate
     */

    Result login(LoginFormDTO loginForm, HttpSession session);

    /**
     * 退出登录
     *@param:  session
     *@return:  Result
     *@Author: yate
     */


}
