package com.campus.activity.controller;

import com.campus.activity.dto.ActivityApprovalRequest;
import com.campus.activity.dto.ActivityApprovalStatistics;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.service.ActivityService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
@Api(tags = "管理员-活动审核")
public class AdminActivityController {

    private static final String ROLE_ADMIN = "admin";

    private final ActivityService activityService;
    private final ActivityMapper activityMapper;
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

    @GetMapping("/pending")
    @ApiOperation("获取待审核活动列表")
    public Result<List<ActivityResponse>> getPendingActivities(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        List<ActivityResponse> activities = activityService.getPendingActivities();
        return Result.success(activities);
    }

    @GetMapping("/approval-status/{status}")
    @ApiOperation("按审核状态获取活动列表")
    public Result<List<ActivityResponse>> getActivitiesByApprovalStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String status) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        List<ActivityResponse> activities = activityService.getActivitiesByApprovalStatus(status);
        return Result.success(activities);
    }

    @PutMapping("/{id}/approve")
    @ApiOperation("审核通过")
    public Result<ActivityResponse> approveActivity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        ActivityResponse activity = activityService.approveActivity(id);
        return Result.success(activity, "活动审核通过");
    }

    @PutMapping("/{id}/reject")
    @ApiOperation("审核拒绝")
    public Result<ActivityResponse> rejectActivity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody ActivityApprovalRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        ActivityResponse activity = activityService.rejectActivity(id, request.getReason());
        return Result.success(activity, "活动审核未通过");
    }

    @GetMapping("/statistics")
    @ApiOperation("获取审核统计信息")
    public Result<ActivityApprovalStatistics> getApprovalStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Long pending = activityMapper.countByApprovalStatus("pending");
        Long approved = activityMapper.countByApprovalStatus("approved");
        Long rejected = activityMapper.countByApprovalStatus("rejected");

        ActivityApprovalStatistics statistics = new ActivityApprovalStatistics();
        statistics.setPending(pending);
        statistics.setApproved(approved);
        statistics.setRejected(rejected);
        statistics.setTotal(pending + approved + rejected);
        statistics.setPendingActivities(activityService.getPendingActivities());

        return Result.success(statistics);
    }
}
