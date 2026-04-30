package com.campus.core.common;

public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 客户端错误 - 4xx
     */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "数据验证失败"),

    /**
     * 服务器错误 - 5xx
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    /**
     * 业务错误 - 自定义
     */
    USER_NOT_FOUND(4001, "用户不存在"),
    USER_ALREADY_EXISTS(4002, "用户名已存在"),
    PASSWORD_ERROR(4003, "密码错误"),
    PASSWORD_INVALID(4004, "密码格式不正确"),
    SECURITY_QUESTION_NOT_SET(4005, "该用户未设置密保问题"),
    SECURITY_QUESTION_INVALID(4006, "无效的密保问题编号"),
    SECURITY_ANSWER_ERROR(4007, "密保答案错误"),
    TOKEN_INVALID(4008, "无效的令牌"),
    TOKEN_EXPIRED(4009, "令牌已过期");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(String customMessage) {
        return customMessage != null ? customMessage : message;
    }
}
