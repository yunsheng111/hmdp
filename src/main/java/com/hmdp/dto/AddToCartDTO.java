package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 添加到购物车请求DTO
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class AddToCartDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;

    /**
     * 商品规格信息JSON
     */
    private String specifications;
}
