package com.hmdp.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 商家评论回复DTO
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
@Data
public class MerchantCommentReplyDTO {
    
    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Length(max = 500, message = "回复内容不能超过500字")
    private String content;
}
