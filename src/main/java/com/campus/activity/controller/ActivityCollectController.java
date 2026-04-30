package com.campus.activity.controller;

import com.campus.activity.entity.ActivityCollect;
import com.campus.activity.service.ActivityCollectService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "活动收藏管理")
@RestController
@RequestMapping("/api/v1/activity-collect")
@RequiredArgsConstructor
public class ActivityCollectController {

    private final ActivityCollectService activityCollectService;
    private final JwtUtils jwtUtils;

    /**
     * 收藏活动
     */
    @PostMapping("/{activityId}")
    @ApiOperation("收藏活动")
    public Result<Map<String, Object>> collect(
            @PathVariable Long activityId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        
        activityCollectService.collectActivity(userId, activityId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("collected", true);
        return Result.success(data, "收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{activityId}")
    @ApiOperation("取消收藏")
    public Result<Map<String, Object>> uncollect(
            @PathVariable Long activityId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        
        activityCollectService.uncollectActivity(userId, activityId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("collected", false);
        return Result.success(data, "取消收藏成功");
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/my")
    @ApiOperation("获取我的收藏列表")
    public Result<List<ActivityCollect>> getMyCollects(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        
        List<ActivityCollect> collects = activityCollectService.getUserCollects(userId);
        return Result.success(collects);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/{activityId}/status")
    @ApiOperation("检查是否已收藏")
    public Result<Map<String, Object>> checkCollectStatus(
            @PathVariable Long activityId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        
        boolean collected = activityCollectService.isCollected(userId, activityId);
        int collectCount = activityCollectService.getCollectCount(activityId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("collected", collected);
        data.put("collectCount", collectCount);
        return Result.success(data);
    }
}
