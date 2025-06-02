package com.hmdp.exception;

/**
 * 评论举报相关异常
 */
public class ReportException extends RuntimeException {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
} 