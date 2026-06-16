package com.campus.activity.controller;

import com.campus.activity.dto.ActivityApprovalRequest;
import com.campus.activity.dto.ActivityApprovalStatistics;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.service.ActivityService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员-活动审核控制器
 * 所有接口需要管理员权限
 */
@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
@Api(tags = "管理员-活动审核")
public class AdminActivityController {

    private final ActivityService activityService;
    private final ActivityMapper activityMapper;

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

    @GetMapping("/pending")
    @ApiOperation("获取待审核活动列表")
    public Result<List<ActivityResponse>> getPendingActivities(HttpServletRequest request) {
        validateAdmin(request);
        List<ActivityResponse> activities = activityService.getPendingActivities();
        return Result.success(activities);
    }

    @GetMapping("/approval-status/{status}")
    @ApiOperation("按审核状态获取活动列表")
    public Result<List<ActivityResponse>> getActivitiesByApprovalStatus(
            HttpServletRequest request,
            @PathVariable String status) {
        validateAdmin(request);
        List<ActivityResponse> activities = activityService.getActivitiesByApprovalStatus(status);
        return Result.success(activities);
    }

    @PutMapping("/{id}/approve")
    @ApiOperation("审核通过")
    public Result<ActivityResponse> approveActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        ActivityResponse activity = activityService.approveActivity(id, adminId);
        return Result.success(activity, "活动审核通过");
    }

    @PutMapping("/{id}/reject")
    @ApiOperation("审核拒绝")
    public Result<ActivityResponse> rejectActivity(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody ActivityApprovalRequest approvalRequest) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        String reason = approvalRequest.getReason();
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST, "拒绝原因不能为空");
        }
        ActivityResponse activity = activityService.rejectActivity(id, reason, adminId);
        return Result.success(activity, "活动审核未通过");
    }

    @GetMapping("/statistics")
    @ApiOperation("获取审核统计信息")
    public Result<ActivityApprovalStatistics> getApprovalStatistics(HttpServletRequest request) {
        validateAdmin(request);
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
