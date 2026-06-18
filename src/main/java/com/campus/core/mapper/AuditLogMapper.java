package com.campus.core.mapper;

import com.campus.core.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Mapper接口
 * 提供审计日志的数据库操作方法
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     * @param auditLog 审计日志实体
     */
    void insert(AuditLog auditLog);

    /**
     * 根据用户ID查询审计日志
     * @param userId 用户ID
     * @return 审计日志列表
     */
    List<AuditLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据操作类型查询审计日志
     * @param operation 操作类型
     * @return 审计日志列表
     */
    List<AuditLog> selectByOperation(@Param("operation") String operation);

    /**
     * 查询最近的审计日志
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<AuditLog> selectRecent(@Param("limit") int limit);

    /**
     * 统计审计日志数量
     * @return 日志总数
     */
    Long countAll();

    /**
     * 删除过期的审计日志
     * @param cutoff 截止时间
     */
    void deleteOldAuditLogs(@Param("cutoff") LocalDateTime cutoff);

    /**
     * 分页查询审计日志（支持多条件筛选）
     * @param userId 操作用户ID（可选）
     * @param operation 操作类型（可选）
     * @param resourceType 资源类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param offset 偏移量
     * @param size 每页数量
     * @return 审计日志列表
     */
    List<AuditLog> selectByConditions(
            @Param("userId") Long userId,
            @Param("operation") String operation,
            @Param("resourceType") String resourceType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("offset") int offset,
            @Param("size") int size);

    /**
     * 按条件统计审计日志数量
     * @param userId 操作用户ID（可选）
     * @param operation 操作类型（可选）
     * @param resourceType 资源类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 符合条件的日志数量
     */
    Long countByConditions(
            @Param("userId") Long userId,
            @Param("operation") String operation,
            @Param("resourceType") String resourceType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计不同操作用户的数量（活跃用户数）
     * @return 不同用户ID的数量
     */
    Long countDistinctUsers();

    /**
     * 统计异常操作数量（响应状态码>=400的操作）
     * @return 异常操作数量
     */
    Long countAbnormalOperations();
}
