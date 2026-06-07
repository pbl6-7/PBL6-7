package com.campus.activity.controller;

import com.campus.activity.entity.ActivityCollect;
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
 * 修复问题3：统一使用拦截器注入的用户信息，与其他控制器保持一致
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
    public Result<Map<String, Object>> collect(
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
        return Result.success(data, "收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{activityId}")
    @ApiOperation("取消收藏")
    public Result<Map<String, Object>> uncollect(
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
        return Result.success(data, "取消收藏成功");
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/my")
    @ApiOperation("获取我的收藏列表")
    public Result<List<ActivityCollect>> getMyCollects(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 查询收藏列表", userId);
        
        List<ActivityCollect> collects = activityCollectService.getUserCollects(userId);
        return Result.success(collects);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/{activityId}/status")
    @ApiOperation("检查是否已收藏")
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
