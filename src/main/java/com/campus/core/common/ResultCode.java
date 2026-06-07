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
     * 认证授权错误 - 1xxx
     */
    AUTHENTICATION_FAILED(1001, "认证失败"),
    AUTHENTICATION_CREDENTIALS_INVALID(1002, "凭证无效"),
    AUTHENTICATION_TOKEN_MISSING(1003, "令牌缺失"),
    AUTHENTICATION_TOKEN_INVALID(1004, "令牌无效"),
    AUTHENTICATION_TOKEN_EXPIRED(1005, "令牌已过期"),
    AUTHENTICATION_ACCOUNT_DISABLED(1006, "账户已被禁用"),
    AUTHENTICATION_ACCOUNT_LOCKED(1007, "账户已被锁定"),
    AUTHENTICATION_ACCOUNT_EXPIRED(1008, "账户已过期"),
    
    AUTHORIZATION_DENIED(1101, "权限不足"),
    AUTHORIZATION_ROLE_INVALID(1102, "角色无效"),
    AUTHORIZATION_PERMISSION_DENIED(1103, "没有操作权限"),
    AUTHORIZATION_RESOURCE_ACCESS_DENIED(1104, "无权访问该资源"),
    AUTHORIZATION_OPERATION_DENIED(1105, "无权执行该操作"),
    
    /**
     * 资源错误 - 2xxx
     */
    RESOURCE_NOT_FOUND(2001, "资源不存在"),
    RESOURCE_ALREADY_EXISTS(2002, "资源已存在"),
    RESOURCE_DELETED(2003, "资源已被删除"),
    RESOURCE_UNAVAILABLE(2004, "资源不可用"),
    RESOURCE_CONFLICT(2005, "资源冲突"),
    DUPLICATE_RESOURCE(2006, "资源重复"),
    
    /**
     * 业务错误 - 自定义 4xxx
     */
    USER_NOT_FOUND(4001, "用户不存在"),
    USER_ALREADY_EXISTS(4002, "用户名已存在"),
    PASSWORD_ERROR(4003, "密码错误"),
    PASSWORD_INVALID(4004, "密码格式不正确"),
    SECURITY_QUESTION_NOT_SET(4005, "该用户未设置密保问题"),
    SECURITY_QUESTION_INVALID(4006, "无效的密保问题编号"),
    SECURITY_ANSWER_ERROR(4007, "密保答案错误"),
    TOKEN_INVALID(4008, "无效的令牌"),
    TOKEN_EXPIRED(4009, "令牌已过期"),
    ACTIVITY_NOT_FOUND(4010, "活动不存在"),
    ACTIVITY_NOT_PENDING(4011, "活动不在待审核状态"),
    INVALID_ROLE(4012, "无效的角色"),
    NOT_ADMIN(4013, "需要管理员权限"),
    NOT_PUBLISHER(4014, "需要发布者权限"),
    
    /**
     * 操作错误 - 5xxx
     */
    OPERATION_NOT_ALLOWED(5001, "操作不允许"),
    OPERATION_FAILED(5002, "操作失败"),
    OPERATION_TIMEOUT(5003, "操作超时"),
    OPERATION_CANCELLED(5004, "操作已取消"),
    OPERATION_IN_PROGRESS(5005, "操作进行中"),
    
    /**
     * 验证错误 - 6xxx
     */
    VALIDATION_FAILED(6001, "验证失败"),
    VALIDATION_FIELD_INVALID(6002, "字段验证失败"),
    VALIDATION_FORMAT_ERROR(6003, "格式错误"),
    VALIDATION_RANGE_ERROR(6004, "范围错误"),
    VALIDATION_LENGTH_ERROR(6005, "长度错误"),
    
    /**
     * 限流错误 - 7xxx
     */
    RATE_LIMIT_EXCEEDED(7001, "请求过于频繁"),
    RATE_LIMIT_IP_EXCEEDED(7002, "IP请求频率超限"),
    RATE_LIMIT_USER_EXCEEDED(7003, "用户请求频率超限");

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
