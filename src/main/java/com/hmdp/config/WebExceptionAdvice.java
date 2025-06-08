package com.hmdp.config;

import com.hmdp.common.Result;
import com.hmdp.exception.CommentException;
import com.hmdp.exception.ReportException;
import com.hmdp.exception.VoucherException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return Result.fail("服务器异常");
    }

    @ExceptionHandler(CommentException.class)
    public Result handleCommentException(CommentException e) {
        log.error("评论异常：{}", e.getMessage(), e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(ReportException.class)
    public Result handleReportException(ReportException e) {
        log.error("举报异常：{}", e.getMessage(), e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(VoucherException.class)
    public Result handleVoucherException(VoucherException e) {
        log.error("优惠券异常：{}", e.getMessage(), e);
        return Result.fail(e.getMessage());
    }
}
