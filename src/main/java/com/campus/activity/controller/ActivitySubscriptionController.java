package com.campus.activity.controller;

import com.campus.activity.dto.SubscriptionDetailResponse;
import com.campus.activity.dto.SubscriptionResponse;
import com.campus.activity.entity.ActivitySubscription;
import com.campus.activity.service.ActivitySubscriptionService;
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
import java.util.stream.Collectors;

/**
 * 活动订阅控制器
 * 提供活动订阅相关的API接口
 */
@Api(tags = "活动订阅管理")
@RestController
@RequestMapping("/api/v1/activity-subscription")
@RequiredArgsConstructor
public class ActivitySubscriptionController {

    private final ActivitySubscriptionService subscriptionService;
    private final JwtUtils jwtUtils;

    /**
     * 订阅活动
     */
    @PostMapping("/{activityId}")
    @ApiOperation("订阅活动")
    public Result<Map<String, Object>> subscribe(
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

        subscriptionService.subscribeActivity(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("subscribed", true);
        data.put("subscriptionCount", subscriptionService.getSubscriptionCount(activityId));
        return Result.success(data, "订阅成功");
    }

    /**
     * 取消订阅
     */
    @DeleteMapping("/{activityId}")
    @ApiOperation("取消订阅")
    public Result<Map<String, Object>> unsubscribe(
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

        subscriptionService.unsubscribeActivity(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("subscribed", false);
        data.put("subscriptionCount", subscriptionService.getSubscriptionCount(activityId));
        return Result.success(data, "取消订阅成功");
    }

    /**
     * 获取用户订阅列表
     */
    @GetMapping("/my")
    @ApiOperation("获取我的订阅列表")
    public Result<List<SubscriptionDetailResponse>> getMySubscriptions(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        List<SubscriptionDetailResponse> subscriptions = subscriptionService.getUserSubscriptionDetails(userId);
        return Result.success(subscriptions);
    }

    /**
     * 检查是否已订阅
     */
    @GetMapping("/{activityId}/status")
    @ApiOperation("检查是否已订阅")
    public Result<Map<String, Object>> checkSubscriptionStatus(
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

        boolean subscribed = subscriptionService.isSubscribed(userId, activityId);
        int subscriptionCount = subscriptionService.getSubscriptionCount(activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("subscribed", subscribed);
        data.put("subscriptionCount", subscriptionCount);
        return Result.success(data);
    }
}
