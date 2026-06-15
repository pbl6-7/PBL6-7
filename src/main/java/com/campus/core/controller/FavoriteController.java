package com.campus.core.controller;

import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.service.FavoriteService;
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
 * 收藏控制器
 * 提供活动收藏相关的API接口
 */
@Api(tags = "活动收藏管理")
@RestController
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     * POST /api/v1/activities/{id}/favorite
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

        favoriteService.addFavorite(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("favorited", true);
        return Result.success(data, "收藏成功");
    }

    /**
     * 取消收藏
     * DELETE /api/v1/activities/{id}/favorite
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

        favoriteService.removeFavorite(userId, activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("favorited", false);
        return Result.success(data, "取消收藏成功");
    }

    /**
     * 检查是否已收藏
     * GET /api/v1/activities/{id}/favorite/status
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

        boolean favorited = favoriteService.isFavorited(userId, activityId);
        int favoriteCount = favoriteService.getFavoriteCount(activityId);

        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        data.put("favoriteCount", favoriteCount);
        return Result.success(data);
    }

    /**
     * 获取用户收藏列表
     * GET /api/v1/users/favorites
     */
    @GetMapping("/api/v1/users/favorites")
    @ApiOperation("获取用户收藏列表")
    public Result<List<Map<String, Object>>> getUserFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 查询收藏列表", userId);

        List<Map<String, Object>> favorites = favoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }
}
