package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.*;

/**
 * 查询优惠券列表请求DTO
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
public class VoucherQueryDTO {
    
    @NotNull(message = "商铺ID不能为空")
    private Long shopId;

    private Integer type;
    
    private Integer status;

    @Size(max = 50, message = "标题搜索关键词长度不能超过50个字符")
    private String title;

    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;
}
