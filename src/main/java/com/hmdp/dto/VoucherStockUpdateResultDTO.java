package com.hmdp.dto;

import lombok.Data;

/**
 * 优惠券库存更新结果DTO
 * 
 * @author yate
 * @since 2024-12-22
 */
@Data
public class VoucherStockUpdateResultDTO {
    
    private Integer currentStock;
    private String operation;
    private Integer changeAmount;
    
    public VoucherStockUpdateResultDTO() {}
    
    public VoucherStockUpdateResultDTO(Integer currentStock, String operation, Integer changeAmount) {
        this.currentStock = currentStock;
        this.operation = operation;
        this.changeAmount = changeAmount;
    }
}
