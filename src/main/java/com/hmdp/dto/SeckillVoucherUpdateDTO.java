package com.hmdp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 更新秒杀优惠券请求DTO
 * 
 * @author yate
 * @since 2024-12-23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SeckillVoucherUpdateDTO extends VoucherUpdateDTO {
    
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @NotNull(message = "秒杀开始时间不能为空")
    private LocalDateTime beginTime;

    @NotNull(message = "秒杀结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "用户限购数量不能为空")
    @Min(value = 1, message = "用户限购数量必须大于0")
    private Integer limitPerUser;
} 