package com.hmdp.dto;

import lombok.Data;

/**
 * <p>
 * 作者选项数据传输对象（用于消息页面下拉选择框）
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class AuthorOptionDTO {
    
    /**
     * 作者ID
     */
    private Long authorId;
    
    /**
     * 作者昵称
     */
    private String authorName;
    
    /**
     * 作者头像
     */
    private String authorIcon;
    
    /**
     * 该作者的未读博客数
     */
    private Integer unreadCount;
}
