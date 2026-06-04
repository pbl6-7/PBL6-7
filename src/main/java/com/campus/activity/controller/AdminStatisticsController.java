package com.campus.activity.controller;

import com.campus.activity.service.AdminStatisticsService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping("/activities")
    @ApiOperation("获取活动统计")
    public Result<Map<String, Object>> getActivityStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Map<String, Object> statistics = adminStatisticsService.getActivityStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/users")
    @ApiOperation("获取用户统计")
    public Result<Map<String, Object>> getUserStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Map<String, Object> statistics = adminStatisticsService.getUserStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/registrations")
    @ApiOperation("获取报名统计")
    public Result<Map<String, Object>> getRegistrationStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Map<String, Object> statistics = adminStatisticsService.getRegistrationStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/daily")
    @ApiOperation("获取每日统计")
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
