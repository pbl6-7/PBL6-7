package com.campus.activity.controller;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.service.ActivityService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Api(tags = "活动管理")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @ApiOperation("发布活动")
    public Result<ActivityResponse> publishActivity(
            HttpServletRequest request,
            @Validated({CreateGroup.class}) @RequestBody ActivityPublishRequest publishRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        String userRole = (String) request.getAttribute("currentUserRole");
        ActivityResponse response = activityService.publishActivity(userId, userRole, publishRequest);
        return Result.success(response, "活动发布成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取活动详情")
    public Result<ActivityResponse> getActivityById(@PathVariable Long id) {
        ActivityResponse response = activityService.getActivityById(id);
        return Result.success(response);
    }

    @GetMapping("/my")
    @ApiOperation("获取我发布的活动列表")
    public Result<List<ActivityResponse>> getMyActivities(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<ActivityResponse> activities = activityService.getActivitiesByPublisher(userId);
        return Result.success(activities);
    }

    @PutMapping("/{id}")
    @ApiOperation("编辑活动")
    public Result<ActivityResponse> updateActivity(
            HttpServletRequest request,
            @PathVariable Long id,
            @Validated({UpdateGroup.class}) @RequestBody ActivityPublishRequest updateRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        ActivityResponse response = activityService.updateActivity(id, userId, updateRequest);
        return Result.success(response, "活动更新成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除活动")
    public Result<Void> deleteActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        activityService.deleteActivity(id, userId);
        return Result.success(null, "活动删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("获取活动列表（带筛选和分页）")
    public Result<ActivityPageResponse> getActivityList(
            HttpServletRequest request,
            @ModelAttribute ActivityQueryRequest queryRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        ActivityPageResponse response = activityService.getActivityList(userId, queryRequest);
        return Result.success(response);
    }
}
