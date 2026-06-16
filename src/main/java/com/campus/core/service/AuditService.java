package com.campus.core.service;

import com.campus.core.entity.AuditLog;
import com.campus.core.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计服务类
 * 提供操作审计和日志记录功能，支持异步持久化到数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    /**
     * 快速记录审计日志（异步持久化）
     *
     * @param userId 用户ID
     * @param targetUserId 目标用户ID（可为null）
     * @param operation 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param result 结果码
     * @param description 描述
     */
    @Async
    public void quickRecord(Long userId, Long targetUserId, String operation,
                          String resourceType, Long resourceId, int result, String description) {
        log.info("审计日志 - 用户:{}, 操作:{}, 资源类型:{}, 资源ID:{}, 结果:{}, 描述:{}",
                userId, operation, resourceType, resourceId, result, description);
        persistAuditLog(userId, targetUserId, operation, resourceType, resourceId, null, null, null, null, result, description, null);
    }

    /**
     * 记录审计日志（完整参数，异步持久化）
     */
    @Async
    public void record(Long userId, Long targetUserId, String operation,
                      String resourceType, Long resourceId, String clientIp,
                      String userAgent, int result, String description) {
        log.info("审计日志 - 用户:{}, 目标用户:{}, 操作:{}, 资源类型:{}, 资源ID:{}, IP:{}, 结果:{}, 描述:{}",
                userId, targetUserId, operation, resourceType, resourceId, clientIp, result, description);
        persistAuditLog(userId, targetUserId, operation, resourceType, resourceId, clientIp, userAgent, null, null, result, description, null);
    }

    /**
     * 持久化审计日志到数据库
     */
    private void persistAuditLog(Long userId, Long targetUserId, String operation,
                                  String resourceType, Long resourceId, String clientIp,
                                  String userAgent, String requestMethod, String requestPath,
                                  int responseStatus, String responseMessage, Integer executionTime) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setTargetUserId(targetUserId);
            auditLog.setOperation(operation);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setClientIp(clientIp);
            auditLog.setUserAgent(userAgent);
            auditLog.setRequestMethod(requestMethod);
            auditLog.setRequestPath(requestPath);
            auditLog.setResponseStatus(responseStatus);
            auditLog.setResponseMessage(responseMessage);
            auditLog.setExecutionTime(executionTime);
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("审计日志持久化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 查询用户的审计日志
     */
    public List<AuditLog> getAuditLogsByUserId(Long userId) {
        return auditLogMapper.selectByUserId(userId);
    }

    /**
     * 查询最近的审计日志
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogMapper.selectRecent(limit);
    }

    /**
     * 统计审计日志总数
     */
    public Long countAll() {
        return auditLogMapper.countAll();
    }
}
