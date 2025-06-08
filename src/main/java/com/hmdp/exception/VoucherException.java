package com.hmdp.exception;

/**
 * 优惠券相关异常
 * 
 * @author yate
 * @since 2024-12-22
 */
public class VoucherException extends RuntimeException {

    public VoucherException(String message) {
        super(message);
    }

    public VoucherException(String message, Throwable cause) {
        super(message, cause);
    }
}
