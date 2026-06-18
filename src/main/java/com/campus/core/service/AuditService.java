package com.campus.core.service;

import com.campus.core.entity.AuditLog;
import com.campus.core.mapper.AuditLogMapper;
import com.campus.core.util.PageUtils;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计服务类
 * 提供操作审计和日志记录功能，支持异步持久化到数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;
    private final UserMapper userMapper;

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
     *
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param operation 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @param result 结果码
     * @param description 描述
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
            /* 根据用户ID查询用户名 */
            if (userId != null) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    auditLog.setUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
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
     *
     * @param userId 用户ID
     * @return 审计日志列表
     */
    public List<AuditLog> getAuditLogsByUserId(Long userId) {
        return auditLogMapper.selectByUserId(userId);
    }

    /**
     * 查询最近的审计日志
     *
     * @param limit 限制数量
     * @return 审计日志列表
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogMapper.selectRecent(limit);
    }

    /**
     * 统计审计日志总数
     *
     * @return 日志总数
     */
    public Long countAll() {
        return auditLogMapper.countAll();
    }

    /**
     * 统计今日操作数量
     *
     * @return 今日操作数量
     */
    public Long countTodayOperations() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return auditLogMapper.countByConditions(null, null, null, todayStart, null);
    }

    /**
     * 统计活跃用户数量（有操作记录的不同用户数）
     *
     * @return 活跃用户数量
     */
    public Long countDistinctUsers() {
        return auditLogMapper.countDistinctUsers();
    }

    /**
     * 统计异常操作数量（响应状态码>=400的操作）
     *
     * @return 异常操作数量
     */
    public Long countAbnormalOperations() {
        return auditLogMapper.countAbnormalOperations();
    }

    /**
     * 分页查询审计日志（支持多条件筛选）
     *
     * @param userId 操作用户ID（可选）
     * @param operation 操作类型（可选）
     * @param resourceType 资源类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 包含分页信息和日志列表的Map
     */
    public Map<String, Object> getAuditLogsByConditions(
            Long userId, String operation, String resourceType,
            LocalDateTime startTime, LocalDateTime endTime,
            Integer page, Integer size) {
        PageUtils.PageParams params = PageUtils.validateAndNormalize(page, size, 20, 100);

        List<AuditLog> logs = auditLogMapper.selectByConditions(
                userId, operation, resourceType, startTime, endTime,
                params.getOffset(), params.getSize());

        Long total = auditLogMapper.countByConditions(
                userId, operation, resourceType, startTime, endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        result.put("page", params.getPage());
        result.put("size", params.getSize());
        result.put("totalPages", PageUtils.calculateTotalPages(total, params.getSize()));
        return result;
    }
}
