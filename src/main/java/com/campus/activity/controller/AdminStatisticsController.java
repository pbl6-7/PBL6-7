package com.campus.activity.controller;

import com.campus.activity.dto.*;
import com.campus.activity.service.AdminStatisticsService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员数据统计控制器
 * 提供系统统计数据查询和报表生成接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@Api(tags = "管理员-数据统计")
public class AdminStatisticsController {

    private static final String ROLE_ADMIN = "admin";

    private final AdminStatisticsService adminStatisticsService;
    private final JwtUtils jwtUtils;

    /**
     * 验证管理员权限
     * 
     * @param token JWT令牌
     */
    private void validateAdminRole(String token) {
        if (!jwtUtils.validateToken(token)) {
            throw new com.campus.core.common.BusinessException(ResultCode.TOKEN_INVALID);
        }
        String role = jwtUtils.getRoleFromToken(token);
        if (!ROLE_ADMIN.equals(role)) {
            throw new com.campus.core.common.BusinessException(ResultCode.NOT_ADMIN);
        }
    }

    /**
     * 获取系统概览统计
     * 包含活动、用户、报名的整体统计数据
     * 
     * @param authorization JWT令牌
     * @return 系统概览统计数据
     */
    @GetMapping("/overview")
    @ApiOperation("获取系统概览统计")
    public Result<OverviewStatisticsDTO> getOverviewStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        OverviewStatisticsDTO statistics = adminStatisticsService.getOverviewStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取活动统计
     * 包含活动总数、状态分布、类型分布、趋势数据等
     * 
     * @param authorization JWT令牌
     * @return 活动统计数据
     */
    @GetMapping("/activities")
    @ApiOperation("获取活动统计")
    public Result<ActivityStatisticsDTO> getActivityStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        ActivityStatisticsDTO statistics = adminStatisticsService.getActivityStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取用户统计
     * 包含用户总数、角色分布、注册趋势、活跃用户等
     * 
     * @param authorization JWT令牌
     * @return 用户统计数据
     */
    @GetMapping("/users")
    @ApiOperation("获取用户统计")
    public Result<UserStatisticsDTO> getUserStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        UserStatisticsDTO statistics = adminStatisticsService.getUserStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取报名统计
     * 包含报名总数、状态分布、趋势数据、热门活动等
     * 
     * @param authorization JWT令牌
     * @return 报名统计数据
     */
    @GetMapping("/registrations")
    @ApiOperation("获取报名统计")
    public Result<RegistrationStatisticsDTO> getRegistrationStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        RegistrationStatisticsDTO statistics = adminStatisticsService.getRegistrationStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取趋势统计（按时间）
     * 支持按月、按周统计活动、用户、报名的趋势数据
     * 
     * @param authorization JWT令牌
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeUnit 时间单位（month、week、day）
     * @return 趋势统计数据
     */
    @GetMapping("/trend")
    @ApiOperation("获取趋势统计（按时间）")
    public Result<Map<String, List<TrendDataDTO>>> getTrendStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @ApiParam(value = "开始日期", example = "2024-01-01T00:00:00") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @ApiParam(value = "结束日期", example = "2024-12-31T23:59:59") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ApiParam(value = "时间单位（month、week、day）", example = "month") 
            @RequestParam(defaultValue = "month") String timeUnit) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        // 如果未指定时间范围，默认查询最近12个月
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(12);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, List<TrendDataDTO>> statistics = 
                adminStatisticsService.getTrendStatistics(startDate, endDate, timeUnit);
        return Result.success(statistics);
    }

    /**
     * 获取热门活动统计
     * 支持按报名人数、收藏人数、浏览次数排序
     * 
     * @param authorization JWT令牌
     * @param limit 返回数量限制（默认10）
     * @param sortBy 排序方式（registration、collection、view）
     * @return 热门活动列表
     */
    @GetMapping("/hot-activities")
    @ApiOperation("获取热门活动统计")
    public Result<List<HotActivityDTO>> getHotActivities(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @ApiParam(value = "返回数量限制", example = "10") 
            @RequestParam(defaultValue = "10") Integer limit,
            @ApiParam(value = "排序方式（registration、collection、view）", example = "registration") 
            @RequestParam(defaultValue = "registration") String sortBy) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        List<HotActivityDTO> statistics = adminStatisticsService.getHotActivities(limit, sortBy);
        return Result.success(statistics);
    }

    /**
     * 清除统计缓存
     * 用于强制刷新统计数据
     * 
     * @param authorization JWT令牌
     * @return 操作结果
     */
    @PostMapping("/clear-cache")
    @ApiOperation("清除统计缓存")
    public Result<Void> clearStatisticsCache(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        adminStatisticsService.clearStatisticsCache();
        log.info("统计缓存已清除");
        return Result.success();
    }

    // ==================== 旧接口保留（兼容性） ====================

    /**
     * 获取每日统计（旧接口）
     * @deprecated 使用 /overview 接口替代
     */
    @Deprecated
    @GetMapping("/daily")
    @ApiOperation("获取每日统计（已废弃）")
    public Result<Map<String, Object>> getDailyStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Map<String, Object> statistics = adminStatisticsService.getDailyStatistics();
        return Result.success(statistics);
    }
}