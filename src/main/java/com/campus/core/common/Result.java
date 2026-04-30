package com.campus.core.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 统一响应结果封装
 *
 * @param <T> 数据类型
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求追踪ID（用于日志排查）
     */
    private String requestId;

    private Result() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = message;
        result.data = data;
        return result;
    }

    /**
     * 错误响应（使用ResultCode枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage();
        return result;
    }

    /**
     * 错误响应（使用ResultCode枚举 + 自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String customMessage) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage(customMessage);
        return result;
    }

    /**
     * 错误响应（使用状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    /**
     * 错误响应（仅消息，默认500）
     */
    public static <T> Result<T> error(String message) {
        return error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(code);
    }

    /**
     * 设置请求追踪ID
     */
    public Result<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
}
