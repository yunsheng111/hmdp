package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 优惠券库存更新请求DTO
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
public class VoucherStockUpdateDTO {
    
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stock;

    @NotBlank(message = "操作类型不能为空")
    private String operation; // add: 增加库存, set: 设置库存
}
