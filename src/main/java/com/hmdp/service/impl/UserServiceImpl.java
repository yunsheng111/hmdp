package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import cn.dev33.satoken.stp.StpUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     *
     * @param: phone session
     * @return: Result
     * @Author: yate
     */
    @Override
    public Result sendCode(String phone) {
        //1. 校验手机号码格式
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2. 如果不符合，返回错误信息
            return Result.fail("手机号码格式不正确");
        }
        log.info("手机号码：{}", phone);

        //3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4.保存验证码到Redis中,LOGIN_CODE_KEY + phone作为key,验证码作为value,设置过期时间为RedisConstants.LOGIN_CODE_TTL分钟,TimeUnit.MINUTES指定单位为分钟
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //5.发送验证码到用户手机 - 确保使用UTF-8编码
        String successMsg = "发送短信验证码成功，验证码：" + code;
        log.info("code:{}",code);
        byte[] msgBytes = successMsg.getBytes(StandardCharsets.UTF_8);
        String encodedMsg = new String(msgBytes, StandardCharsets.UTF_8);
        log.debug(encodedMsg);
        //6.返回成功信息 - 确保使用UTF-8编码
        String returnMsg = "验证码发送成功";
        byte[] returnMsgBytes = returnMsg.getBytes(StandardCharsets.UTF_8);
        String encodedReturnMsg = new String(returnMsgBytes, StandardCharsets.UTF_8);
        return Result.success(encodedReturnMsg);
    }

    /**
     * 登录
     *
     * @param: loginForm session
     * @return: Result
     * @Author: yate
     */
    @Override
    public Result login(LoginFormDTO loginForm) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }

        User user = null;

        // 3.判断登录方式：检查是否为密码登录
        String password = loginForm.getPassword();
        String code = loginForm.getCode();

        if (password != null && !password.trim().isEmpty()) {
            // 密码登录模式
            log.info("用户选择密码登录，手机号：{}", phone);

            // 3.1.根据手机号查询用户
            user = query().eq("phone", phone).one();

            // 3.2.判断用户是否存在
            if (user == null) {
                return Result.fail("用户不存在，请先注册或使用验证码登录");
            }

            // 3.3.验证密码
            if (!password.equals(user.getPassword())) {
                return Result.fail("密码错误");
            }

        } else if (code != null && !code.trim().isEmpty()) {
            // 验证码登录模式
            log.info("用户选择验证码登录，手机号：{}", phone);

            // 3.1.从redis获取验证码并校验
            String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
            if (cacheCode == null || !cacheCode.equals(code)) {
                // 不一致，报错
                return Result.fail("验证码错误");
            }

            // 3.2.根据手机号查询用户
            user = query().eq("phone", phone).one();

            // 3.3.判断用户是否存在
            if (user == null) {
                // 不存在，创建新用户并保存
                user = createUserWithPhone(phone);
            }

        } else {
            // 既没有密码也没有验证码
            return Result.fail("请输入密码或验证码");
        }

        // 4.生成登录会话和token
        // Sa-Token V Начало: Интеграция создания сеанса Sa-Token
        StpUtil.login(user.getId());
        log.info("Sa-Token session created for user: {}", user.getId());
        // Sa-Token V Конец: Интеграция создания сеанса Sa-Token

        // 5.保存用户信息到 redis中
        // 5.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 5.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 5.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 5.4.设置token有效期,LOGIN_USER_TTL分钟,TimeUnit.MINUTES指定单位为分钟
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 6.返回token
        return Result.success(token);
    }



    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + phone);
        user.setPassword(RandomUtil.randomString(8));
        // 2.保存用户
        this.save(user);
        return user;
    }

    /**
     * 退出登录
     * @param:
     * @return:
     * @Author: yate
     */

    @Override
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (token == null) {
            return Result.fail("未登录");
        }
        // 从 Redis 中删除 token
        stringRedisTemplate.delete(LOGIN_USER_KEY + token);
        return Result.success("退出成功");
    }

    /**
     * 根据id获取用户
     *
     * @param userId 用户id
     * @return Result
     */
    @Override
    public Result getUserById(Long userId) {
        // 查询用户
        User user = getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        // 将User对象转为UserDTO对象
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.success(userDTO);
    }

}
