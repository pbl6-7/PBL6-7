package com.campus.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志实体类
 * 用于记录系统操作审计信息
 */
@Data
public class AuditLog {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 目标用户ID
     */
    private Long targetUserId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应消息
     */
    private String responseMessage;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 执行时间(ms)
     */
    private Integer executionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
