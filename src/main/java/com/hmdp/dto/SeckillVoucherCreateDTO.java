package com.hmdp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 创建秒杀券请求DTO
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillVoucherCreateDTO extends VoucherCreateDTO {
    
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量必须大于0")
    private Integer stock;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime beginTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    private LocalDateTime endTime;

    @Min(value = 1, message = "每人限购数量必须大于0")
    private Integer limitPerUser = 1;
}
