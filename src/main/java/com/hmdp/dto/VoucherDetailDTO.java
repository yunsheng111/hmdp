package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券详情响应DTO
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
public class VoucherDetailDTO {
    
    private Long id;
    private Long shopId;
    private Integer type;
    private String title;
    private String subTitle;
    private String rules;
    private Long payValue;
    private Long actualValue;
    private Long minAmount;
    private Integer validDays;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 统计信息
    private Integer receivedCount;
    private Integer usedCount;
    private Integer remainingCount;

    // 秒杀券特有字段
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer limitPerUser;
    
    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 1:
                return "上架";
            case 2:
                return "下架";
            case 3:
                return "过期";
            default:
                return "未知";
        }
    }
}
