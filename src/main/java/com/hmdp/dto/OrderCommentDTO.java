package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 订单评价请求DTO
 *
 * @author yate
 * @since 2024-12-23
 */
@Data
public class OrderCommentDTO {

    /**
     * 评分(1-5)
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer rating;

    /**
     * 评价内容
     */
    @Size(max = 500, message = "评价内容不能超过500字")
    private String content;

    /**
     * 评价图片URL列表
     */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
}
