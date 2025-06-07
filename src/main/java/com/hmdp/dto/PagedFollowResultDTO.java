package com.hmdp.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页关注结果DTO
 * 用于包装分页查询的关注/粉丝列表结果
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
public class PagedFollowResultDTO<T> {
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 总数量
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer currentPage;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 构造函数
     */
    public PagedFollowResultDTO() {}
    
    /**
     * 构造函数
     * @param list 数据列表
     * @param total 总数量
     * @param currentPage 当前页码
     * @param pageSize 每页大小
     */
    public PagedFollowResultDTO(List<T> list, Long total, Integer currentPage, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
    
    /**
     * 计算总页数
     * @return 总页数
     */
    public Integer getTotalPages() {
        if (total == null || pageSize == null || pageSize == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }
}
