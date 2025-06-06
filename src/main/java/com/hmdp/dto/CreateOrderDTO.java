package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建订单请求DTO
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class CreateOrderDTO {

    /**
     * 配送地址
     */
    @NotBlank(message = "配送地址不能为空")
    private String address;

    /**
     * 支付方式：1-余额支付，2-支付宝，3-微信
     */
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    /**
     * 备注信息
     */
    private String remark;
}
