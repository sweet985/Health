package com.example.health.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(200, "操作成功", data);
    }

    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<>(500, message, null);
    }

    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<>(403, "没有权限", data);
    }

    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<>(401, "暂未登录或token已经过期", data);
    }
}
