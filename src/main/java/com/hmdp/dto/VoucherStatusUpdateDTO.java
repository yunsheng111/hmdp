package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 优惠券状态更新请求DTO
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
public class VoucherStatusUpdateDTO {
    
    @NotNull(message = "状态不能为空")
    private Integer status;
}
