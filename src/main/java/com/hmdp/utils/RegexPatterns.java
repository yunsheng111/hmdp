package com.hmdp.utils;

/**
 * @author 虎哥
 */
public abstract class RegexPatterns {
    /**
     * 手机号正则
     * 手机号以数字1开头。
     * 第二位数字限定为3、4、5、6、7、8、9。
     * 根据第二位数字的不同，第三位数字有特定范围。
     * 后续8位为任意数字。
     */
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
    /**
     * 邮箱正则
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    /**
     * 密码正则。4~32位的字母、数字、下划线
     */
    public static final String PASSWORD_REGEX = "^\\w{4,32}$";
    /**
     * 验证码正则, 6位数字或字母
     */
    public static final String VERIFY_CODE_REGEX = "^[a-zA-Z\\d]{6}$";

}
