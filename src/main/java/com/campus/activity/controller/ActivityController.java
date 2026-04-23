package com.campus.activity.controller;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.service.ActivityService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Api(tags = "活动管理")
public class ActivityController {

    private final ActivityService activityService;
    private final JwtUtils jwtUtils;

    @PostMapping
    @ApiOperation("发布活动")
    public Result<ActivityResponse> publishActivity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ActivityPublishRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        ActivityResponse response = activityService.publishActivity(userId, request);
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
    public Result<List<ActivityResponse>> getMyActivities(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<ActivityResponse> activities = activityService.getActivitiesByPublisher(userId);
        return Result.success(activities);
    }

    @PutMapping("/{id}")
    @ApiOperation("编辑活动")
    public Result<ActivityResponse> updateActivity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ActivityPublishRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        ActivityResponse response = activityService.updateActivity(id, userId, request);
        return Result.success(response, "活动更新成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除活动")
    public Result<Void> deleteActivity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        activityService.deleteActivity(id, userId);
        return Result.success(null, "活动删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("获取活动列表（带筛选和分页）")
    public Result<ActivityPageResponse> getActivityList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @ModelAttribute ActivityQueryRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        ActivityPageResponse response = activityService.getActivityList(userId, request);
        return Result.success(response);
    }
}
