package com.hmdp.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>
 * 商家回复评论数据传输对象
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class CommentReplyDTO {
    
    /**
     * 评论ID
     */
    @NotNull(message = "评论ID不能为空")
    private Long commentId;
    
    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(min = 2, max = 200, message = "回复内容长度必须在2-200个字符之间")
    private String content;
}
