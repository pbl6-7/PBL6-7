package com.campus.activity.controller;

import com.campus.activity.dto.ActivityApprovalRequest;
import com.campus.activity.dto.ActivityApprovalStatistics;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.service.ActivityService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/activities")
@RequiredArgsConstructor
@Api(tags = "管理员-活动审核")
public class AdminActivityController {

    private final ActivityService activityService;
    private final ActivityMapper activityMapper;

    @GetMapping("/pending")
    @ApiOperation("获取待审核活动列表")
    public Result<List<ActivityResponse>> getPendingActivities(HttpServletRequest request) {
        List<ActivityResponse> activities = activityService.getPendingActivities();
        return Result.success(activities);
    }

    @GetMapping("/approval-status/{status}")
    @ApiOperation("按审核状态获取活动列表")
    public Result<List<ActivityResponse>> getActivitiesByApprovalStatus(
            HttpServletRequest request,
            @PathVariable String status) {
        List<ActivityResponse> activities = activityService.getActivitiesByApprovalStatus(status);
        return Result.success(activities);
    }

    @PutMapping("/{id}/approve")
    @ApiOperation("审核通过")
    public Result<ActivityResponse> approveActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long adminId = (Long) request.getAttribute("currentUserId");
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        ActivityResponse activity = activityService.approveActivity(id, adminId);
        return Result.success(activity, "活动审核通过");
    }

    @PutMapping("/{id}/reject")
    @ApiOperation("审核拒绝")
    public Result<ActivityResponse> rejectActivity(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody ActivityApprovalRequest approvalRequest) {
        Long adminId = (Long) request.getAttribute("currentUserId");
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
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
