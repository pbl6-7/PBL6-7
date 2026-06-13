package com.campus.core.common;

import java.util.Map;

/**
 * 业务异常类
 * 用于抛出业务相关的异常，配合 GlobalExceptionHandler 返回统一格式的错误响应
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 错误详情
     */
    private final Map<String, Object> details;

    /**
     * 构造业务异常（使用ResultCode枚举）
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.details = null;
    }

    /**
     * 构造业务异常（使用ResultCode枚举 + 自定义消息）
     */
    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
        this.details = null;
    }

    /**
     * 构造业务异常（使用状态码和消息）
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = null;
    }

    /**
     * 构造业务异常（仅消息，默认使用服务器错误码）
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
        this.details = null;
    }

    /**
     * 构造业务异常（带详情）
     */
    public BusinessException(ResultCode resultCode, String customMessage, Map<String, Object> details) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
        this.details = details;
    }

    /**
     * 构造业务异常（带详情）
     */
    public BusinessException(Integer code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
