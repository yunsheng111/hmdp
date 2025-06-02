package com.hmdp.exception;

/**
 * 评论相关异常
 */
public class CommentException extends RuntimeException {

    public CommentException(String message) {
        super(message);
    }

    public CommentException(String message, Throwable cause) {
        super(message, cause);
    }
} 