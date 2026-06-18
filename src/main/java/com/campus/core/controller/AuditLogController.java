package com.campus.core.controller;

import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import com.campus.core.entity.AuditLog;
import com.campus.core.service.AuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作日志控制器
 * 提供操作日志的查询和展示API，管理员可查看所有日志，普通用户可查看自己的操作记录
 */
@Api(tags = "操作日志管理")
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditService auditService;

    /**
     * 验证管理员权限
     *
     * @param request HTTP请求对象
     */
    private void validateAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("currentUserRole");
        if (!UserRoleConstants.ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    /**
     * 分页查询操作日志（管理员）
     * 支持按用户ID、操作类型、资源类型、时间范围筛选
     *
     * @param request HTTP请求对象
     * @param userId 操作用户ID（可选）
     * @param operation 操作类型（可选）
     * @param resourceType 资源类型（可选）
     * @param startTime 开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @param page 页码（默认1）
     * @param size 每页数量（默认20）
     * @return 分页操作日志数据
     */
    @GetMapping
    @ApiOperation("分页查询操作日志（管理员）")
    public Result<Map<String, Object>> getAuditLogs(
            HttpServletRequest request,
            @ApiParam("操作用户ID") @RequestParam(required = false) Long userId,
            @ApiParam("操作类型") @RequestParam(required = false) String operation,
            @ApiParam("资源类型") @RequestParam(required = false) String resourceType,
            @ApiParam("开始时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "20") Integer size) {
        validateAdmin(request);
        log.info("管理员查询操作日志: userId={}, operation={}, resourceType={}, startTime={}, endTime={}",
                userId, operation, resourceType, startTime, endTime);

        Map<String, Object> result = auditService.getAuditLogsByConditions(
                userId, operation, resourceType, startTime, endTime, page, size);
        return Result.success(result);
    }

    /**
     * 获取最近操作日志（管理员）
     *
     * @param request HTTP请求对象
     * @param limit 限制数量（默认50）
     * @return 最近的操作日志列表
     */
    @GetMapping("/recent")
    @ApiOperation("获取最近操作日志（管理员）")
    public Result<List<AuditLog>> getRecentAuditLogs(
            HttpServletRequest request,
            @ApiParam("限制数量") @RequestParam(defaultValue = "50") Integer limit) {
        validateAdmin(request);
        List<AuditLog> logs = auditService.getRecentAuditLogs(limit);
        return Result.success(logs);
    }

    /**
     * 查询当前用户的操作日志
     *
     * @param request HTTP请求对象
     * @return 当前用户的操作日志列表
     */
    @GetMapping("/my")
    @ApiOperation("查询当前用户的操作日志")
    public Result<List<AuditLog>> getMyAuditLogs(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        List<AuditLog> logs = auditService.getAuditLogsByUserId(userId);
        return Result.success(logs);
    }

    /**
     * 获取操作日志统计信息（管理员）
     * 返回总操作数、今日操作、活跃用户、异常操作等统计数据
     *
     * @param request HTTP请求对象
     * @return 统计信息，包含 totalOperations、todayOperations、activeUsers、abnormalOperations
     */
    @GetMapping("/stats")
    @ApiOperation("获取操作日志统计信息（管理员）")
    public Result<Map<String, Object>> getAuditLogStats(HttpServletRequest request) {
        validateAdmin(request);
        Long totalOperations = auditService.countAll();
        Long todayOperations = auditService.countTodayOperations();
        Long activeUsers = auditService.countDistinctUsers();
        Long abnormalOperations = auditService.countAbnormalOperations();

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalOperations", totalOperations);
        stats.put("todayOperations", todayOperations);
        stats.put("activeUsers", activeUsers);
        stats.put("abnormalOperations", abnormalOperations);
        return Result.success(stats);
    }
}
