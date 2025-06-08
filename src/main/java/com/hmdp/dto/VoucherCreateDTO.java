package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.*;

/**
 * 创建优惠券请求DTO
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
public class VoucherCreateDTO {
    
    @NotNull(message = "商铺ID不能为空")
    private Long shopId;

    @NotNull(message = "优惠券类型不能为空")
    private Integer type;

    @NotBlank(message = "优惠券标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;

    @Size(max = 200, message = "副标题长度不能超过200个字符")
    private String subTitle;

    @NotBlank(message = "使用规则不能为空")
    @Size(max = 1000, message = "使用规则长度不能超过1000个字符")
    private String rules;

    @NotNull(message = "支付金额不能为空")
    @Min(value = 0, message = "支付金额不能为负数")
    private Long payValue;

    @NotNull(message = "抵扣金额不能为空")
    @Min(value = 0, message = "抵扣金额不能为负数")
    private Long actualValue;

    @NotNull(message = "最低消费金额不能为空")
    @Min(value = 0, message = "最低消费金额不能为负数")
    private Long minAmount;

    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数必须大于0")
    private Integer validDays;
}
