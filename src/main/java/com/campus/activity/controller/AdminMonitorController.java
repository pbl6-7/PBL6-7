package com.campus.activity.controller;

import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.service.AdminMonitorService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.UserResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/monitor")
@RequiredArgsConstructor
@Api(tags = "管理员-系统监控")
public class AdminMonitorController {

    private static final String ROLE_ADMIN = "admin";

    private final AdminMonitorService adminMonitorService;
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

    @GetMapping("/status")
    @ApiOperation("获取系统状态")
    public Result<Map<String, Object>> getSystemStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Map<String, Object> status = adminMonitorService.getSystemStatus();
        return Result.success(status);
    }

    @GetMapping("/metrics")
    @ApiOperation("获取系统指标")
    public Result<Map<String, Object>> getSystemMetrics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Map<String, Object> metrics = adminMonitorService.getSystemMetrics();
        return Result.success(metrics);
    }

    @GetMapping("/recent-activities")
    @ApiOperation("获取最近活动")
    public Result<List<ActivityResponse>> getRecentActivities(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        List<ActivityResponse> activities = adminMonitorService.getRecentActivities();
        return Result.success(activities);
    }

    @GetMapping("/recent-users")
    @ApiOperation("获取最近用户")
    public Result<List<UserResponse>> getRecentUsers(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        List<UserResponse> users = adminMonitorService.getRecentUsers();
        return Result.success(users);
    }
}
