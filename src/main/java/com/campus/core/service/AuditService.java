package com.campus.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审计服务类
 * 提供操作审计和日志记录功能
 */
@Slf4j
@Service
public class AuditService {

    /**
     * 快速记录审计日志
     *
     * @param userId 用户ID
     * @param targetUserId 目标用户ID（可为null）
     * @param operation 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param result 结果码
     * @param description 描述
     */
    public void quickRecord(Long userId, Long targetUserId, String operation,
                          String resourceType, Long resourceId, int result, String description) {
        log.info("审计日志 - 用户:{}, 操作:{}, 资源类型:{}, 资源ID:{}, 结果:{}, 描述:{}",
                userId, operation, resourceType, resourceId, result, description);
        // 实际实现可能需要保存到数据库
    }

    /**
     * 记录审计日志（完整参数）
     */
    public void record(Long userId, Long targetUserId, String operation,
                      String resourceType, Long resourceId, String ipAddress,
                      String userAgent, int result, String description) {
        log.info("审计日志 - 用户:{}, 目标用户:{}, 操作:{}, 资源类型:{}, 资源ID:{}, IP:{}, 结果:{}, 描述:{}",
                userId, targetUserId, operation, resourceType, resourceId, ipAddress, result, description);
    }
}
