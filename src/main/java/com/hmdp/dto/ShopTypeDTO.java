package com.hmdp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>
 * 商户分类请求DTO
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
@ApiModel(description = "商户分类请求DTO")
public class ShopTypeDTO {

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称", required = true, example = "美食")
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 10, message = "分类名称长度在2到10个字符之间")
    private String name;

    /**
     * 图标URL
     */
    @ApiModelProperty(value = "图标URL", required = true, example = "/types/ms.png")
    @NotBlank(message = "图标URL不能为空")
    @Size(max = 255, message = "图标URL长度不能超过255个字符")
    private String icon;

    /**
     * 排序值
     */
    @ApiModelProperty(value = "排序值", required = true, example = "1")
    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
