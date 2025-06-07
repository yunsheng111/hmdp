package com.hmdp.dto;

import com.hmdp.entity.Blog;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 消息页面数据传输对象
 * </p>
 *
 * @author yate
 * @since 2024-12-22
 */
@Data
public class MessagePageDTO {
    
    /**
     * 总未读博客数
     */
    private Integer totalUnreadCount;
    
    /**
     * 作者选项列表（用于下拉选择框）
     */
    private List<AuthorOptionDTO> authorOptions;
    
    /**
     * 最近的未读博客列表（默认显示）
     */
    private List<Blog> recentUnreadBlogs;
}
