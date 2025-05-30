package com.hmdp.utils;

import com.hmdp.dto.MerchantDTO;

public class MerchantHolder {
    private static final ThreadLocal<MerchantDTO> tl = new ThreadLocal<>();

    public static void saveMerchant(MerchantDTO merchant){
        tl.set(merchant);
    }

    public static MerchantDTO getMerchant(){
        return tl.get();
    }

    public static void removeMerchant(){
        tl.remove();
    }
} 