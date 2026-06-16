package com.campus.activity.controller;

import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.service.AdminMonitorService;
import com.campus.activity.service.CacheService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import com.campus.user.dto.UserResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 管理员-系统监控控制器
 * 所有接口需要管理员权限
 */
@RestController
@RequestMapping("/api/admin/monitor")
@RequiredArgsConstructor
@Api(tags = "管理员-系统监控")
@Slf4j
public class AdminMonitorController {

    private final AdminMonitorService adminMonitorService;
    private final CacheService cacheService;

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

    @GetMapping("/status")
    @ApiOperation("获取系统状态")
    public Result<Map<String, Object>> getSystemStatus(HttpServletRequest request) {
        validateAdmin(request);
        Map<String, Object> status = adminMonitorService.getSystemStatus();
        return Result.success(status);
    }

    @GetMapping("/metrics")
    @ApiOperation("获取系统指标")
    public Result<Map<String, Object>> getSystemMetrics(HttpServletRequest request) {
        validateAdmin(request);
        Map<String, Object> metrics = adminMonitorService.getSystemMetrics();
        return Result.success(metrics);
    }

    @GetMapping("/recent-activities")
    @ApiOperation("获取最近活动")
    public Result<List<ActivityResponse>> getRecentActivities(HttpServletRequest request) {
        validateAdmin(request);
        List<ActivityResponse> activities = adminMonitorService.getRecentActivities();
        return Result.success(activities);
    }

    @GetMapping("/recent-users")
    @ApiOperation("获取最近用户")
    public Result<List<UserResponse>> getRecentUsers(HttpServletRequest request) {
        validateAdmin(request);
        List<UserResponse> users = adminMonitorService.getRecentUsers();
        return Result.success(users);
    }

    /**
     * 获取缓存信息
     */
    @GetMapping("/cache")
    @ApiOperation("获取缓存信息")
    public Result<Map<String, Object>> getCacheInfo(HttpServletRequest request) {
        validateAdmin(request);
        Map<String, Object> cacheInfo = adminMonitorService.getCacheInfo();
        return Result.success(cacheInfo);
    }

    /**
     * 清除系统缓存
     */
    @DeleteMapping("/cache/clear")
    @ApiOperation("清除系统缓存")
    public Result<Void> clearCache(HttpServletRequest request) {
        validateAdmin(request);
        cacheService.clearAll();
        log.info("系统缓存已清除");
        return Result.success(null, "缓存清除成功");
    }
}
