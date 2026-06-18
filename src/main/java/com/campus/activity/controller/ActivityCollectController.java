package com.campus.activity.controller;

import com.campus.activity.dto.CollectDetailResponse;
import com.campus.activity.service.ActivityCollectService;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动收藏控制器
 * 提供活动收藏相关的API接口
 */
@Api(tags = "活动收藏管理")
@RestController
@RequestMapping("/api/v1/activity-collect")
@RequiredArgsConstructor
@Slf4j
public class ActivityCollectController {

    private final ActivityCollectService activityCollectService;

    /**
     * 收藏活动
     */
    @PostMapping("/{activityId}")
    @ApiOperation("收藏活动")
    public Result<Map<String, Object>> collectActivity(
            HttpServletRequest request,
            @PathVariable Long activityId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 收藏活动 {}", userId, activityId);

        activityCollectService.collectActivity(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("collected", true);
        data.put("collectCount", activityCollectService.getCollectCount(activityId));
        return Result.success(data, "收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{activityId}")
    @ApiOperation("取消收藏")
    public Result<Map<String, Object>> uncollectActivity(
            HttpServletRequest request,
            @PathVariable Long activityId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 取消收藏活动 {}", userId, activityId);

        activityCollectService.uncollectActivity(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("collected", false);
        data.put("collectCount", activityCollectService.getCollectCount(activityId));
        return Result.success(data, "取消收藏成功");
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my")
    @ApiOperation("获取我的收藏列表")
    public Result<List<CollectDetailResponse>> getMyCollects(
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 查询收藏列表", userId);

        List<CollectDetailResponse> collects = activityCollectService.getUserCollectDetails(userId);
        return Result.success(collects);
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/{activityId}/status")
    @ApiOperation("检查收藏状态")
    public Result<Map<String, Object>> checkCollectStatus(
            HttpServletRequest request,
            @PathVariable Long activityId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        boolean collected = activityCollectService.isCollected(userId, activityId);
        int collectCount = activityCollectService.getCollectCount(activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("collected", collected);
        data.put("collectCount", collectCount);
        return Result.success(data);
    }
}
