package com.campus.activity.controller;

import com.campus.activity.dto.*;
import com.campus.activity.service.AdminStatisticsService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
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
 * 管理员数据统计控制器
 * 提供系统统计数据查询和报表生成接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@Api(tags = "管理员-数据统计")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    /**
     * 验证管理员权限
     */
    private void validateAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String role = (String) request.getAttribute("currentUserRole");
        if (userId == null || role == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        if (!UserRoleConstants.ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    @GetMapping("/overview")
    @ApiOperation("获取系统概览统计")
    public Result<OverviewStatisticsDTO> getOverviewStatistics(HttpServletRequest request) {
        validateAdmin(request);
        OverviewStatisticsDTO statistics = adminStatisticsService.getOverviewStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/activities")
    @ApiOperation("获取活动统计")
    public Result<ActivityStatisticsDTO> getActivityStatistics(HttpServletRequest request) {
        validateAdmin(request);
        ActivityStatisticsDTO statistics = adminStatisticsService.getActivityStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/users")
    @ApiOperation("获取用户统计")
    public Result<UserStatisticsDTO> getUserStatistics(HttpServletRequest request) {
        validateAdmin(request);
        UserStatisticsDTO statistics = adminStatisticsService.getUserStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/registrations")
    @ApiOperation("获取报名统计")
    public Result<RegistrationStatisticsDTO> getRegistrationStatistics(HttpServletRequest request) {
        validateAdmin(request);
        RegistrationStatisticsDTO statistics = adminStatisticsService.getRegistrationStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/trend")
    @ApiOperation("获取趋势统计（按时间）")
    public Result<Map<String, List<TrendDataDTO>>> getTrendStatistics(
            HttpServletRequest request,
            @ApiParam(value = "开始日期", example = "2024-01-01T00:00:00") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @ApiParam(value = "结束日期", example = "2024-12-31T23:59:59") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ApiParam(value = "时间单位（month、week、day）", example = "month") 
            @RequestParam(defaultValue = "month") String timeUnit) {
        validateAdmin(request);

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

    @GetMapping("/hot-activities")
    @ApiOperation("获取热门活动统计")
    public Result<List<HotActivityDTO>> getHotActivities(
            HttpServletRequest request,
            @ApiParam(value = "返回数量限制", example = "10") 
            @RequestParam(defaultValue = "10") Integer limit,
            @ApiParam(value = "排序方式（registration、collection、view）", example = "registration") 
            @RequestParam(defaultValue = "registration") String sortBy) {
        validateAdmin(request);
        List<HotActivityDTO> statistics = adminStatisticsService.getHotActivities(limit, sortBy);
        return Result.success(statistics);
    }

    @PostMapping("/clear-cache")
    @ApiOperation("清除统计缓存")
    public Result<Void> clearStatisticsCache(HttpServletRequest request) {
        validateAdmin(request);
        adminStatisticsService.clearStatisticsCache();
        log.info("统计缓存已清除");
        return Result.success();
    }

    /**
     * 获取每日统计（旧接口）
     * @deprecated 使用 /overview 接口替代
     */
    @Deprecated
    @GetMapping("/daily")
    @ApiOperation("获取每日统计（已废弃）")
    public Result<Map<String, Object>> getDailyStatistics(HttpServletRequest request) {
        validateAdmin(request);
        Map<String, Object> statistics = adminStatisticsService.getDailyStatistics();
        return Result.success(statistics);
    }
}
