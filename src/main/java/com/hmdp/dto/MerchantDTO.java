package com.hmdp.dto;

import lombok.Data;

@Data
public class MerchantDTO {
    private Long id;
    private String name;
    private String account;
    private String phone;
    private String avatar;
    private String description;
    private Integer status;
} 