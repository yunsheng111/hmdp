package com.hmdp.dto;

import lombok.Data;

@Data
public class MerchantLoginFormDTO {
    private String account;
    private String password;
    private String phone;
    private String code;
}