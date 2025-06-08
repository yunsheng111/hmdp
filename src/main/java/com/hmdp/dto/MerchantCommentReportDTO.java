package com.hmdp.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 商家评论举报DTO
 * </p>
 *
 * @author Claude 4.0 Sonnet
 * @since 2024-12-23
 */
@Data
public class MerchantCommentReportDTO {
    
    /**
     * 举报原因
     */
    @NotBlank(message = "举报原因不能为空")
    @Length(max = 200, message = "举报原因不能超过200字")
    private String reason;
}
