package com.campus.core.controller;

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
 * 收藏控制器（兼容旧API路径）
 * 内部委托给 ActivityCollectService，统一使用 activity_collect 表
 */
@Api(tags = "活动收藏管理")
@RestController
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final ActivityCollectService activityCollectService;

    /**
     * 添加收藏
     * POST /api/v1/activities/{id}/favorite
     *
     * @param request HTTP请求对象
     * @param activityId 活动ID
     * @return 收藏结果
     */
    @PostMapping("/api/v1/activities/{id}/favorite")
    @ApiOperation("添加收藏")
    public Result<Map<String, Object>> addFavorite(
            HttpServletRequest request,
            @PathVariable("id") Long activityId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 收藏活动 {}", userId, activityId);

        activityCollectService.collectActivity(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("favorited", true);
        data.put("collectCount", activityCollectService.getCollectCount(activityId));
        return Result.success(data, "收藏成功");
    }

    /**
     * 取消收藏
     * DELETE /api/v1/activities/{id}/favorite
     *
     * @param request HTTP请求对象
     * @param activityId 活动ID
     * @return 取消收藏结果
     */
    @DeleteMapping("/api/v1/activities/{id}/favorite")
    @ApiOperation("取消收藏")
    public Result<Map<String, Object>> removeFavorite(
            HttpServletRequest request,
            @PathVariable("id") Long activityId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 取消收藏活动 {}", userId, activityId);

        activityCollectService.uncollectActivity(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("favorited", false);
        data.put("collectCount", activityCollectService.getCollectCount(activityId));
        return Result.success(data, "取消收藏成功");
    }

    /**
     * 检查是否已收藏
     * GET /api/v1/activities/{id}/favorite/status
     *
     * @param request HTTP请求对象
     * @param activityId 活动ID
     * @return 收藏状态
     */
    @GetMapping("/api/v1/activities/{id}/favorite/status")
    @ApiOperation("检查是否已收藏")
    public Result<Map<String, Object>> checkFavoriteStatus(
            HttpServletRequest request,
            @PathVariable("id") Long activityId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        boolean collected = activityCollectService.isCollected(userId, activityId);
        int collectCount = activityCollectService.getCollectCount(activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("favorited", collected);
        data.put("collectCount", collectCount);
        return Result.success(data);
    }

    /**
     * 获取用户收藏列表
     * GET /api/v1/users/favorites
     *
     * @param request HTTP请求对象
     * @return 用户收藏列表
     */
    @GetMapping("/api/v1/users/favorites")
    @ApiOperation("获取用户收藏列表")
    public Result<List<CollectDetailResponse>> getUserFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 查询收藏列表", userId);

        List<CollectDetailResponse> collects = activityCollectService.getUserCollectDetails(userId);
        return Result.success(collects);
    }
}
