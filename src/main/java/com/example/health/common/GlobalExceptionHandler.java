package com.example.health.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public CommonResult<String> handleRuntimeException(RuntimeException e) {
        return CommonResult.failed(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<String> handleException(Exception e) {
        e.printStackTrace();
        return CommonResult.failed("系统内部错误");
    }
}
