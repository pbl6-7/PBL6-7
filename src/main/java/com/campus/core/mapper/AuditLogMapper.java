package com.campus.core.mapper;

import com.campus.core.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Mapper接口
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     */
    void insert(AuditLog auditLog);

    /**
     * 根据用户ID查询审计日志
     */
    List<AuditLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据操作类型查询审计日志
     */
    List<AuditLog> selectByOperation(@Param("operation") String operation);

    /**
     * 查询最近的审计日志
     */
    List<AuditLog> selectRecent(@Param("limit") int limit);

    /**
     * 统计审计日志数量
     */
    Long countAll();

    /**
     * 删除过期的审计日志
     */
    void deleteOldAuditLogs(@Param("cutoff") LocalDateTime cutoff);
}
