package com.campus.activity.controller;

import com.campus.activity.dto.NotificationPageResponse;
import com.campus.activity.service.NotificationService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器
 * 提供通知相关的API接口
 */
@Api(tags = "通知管理")
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;

    /**
     * 获取我的通知列表
     */
    @GetMapping("/my")
    @ApiOperation("获取我的通知列表")
    public Result<NotificationPageResponse> getMyNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        NotificationPageResponse notifications = notificationService.getUserNotifications(userId, page, size);
        return Result.success(notifications);
    }

    /**
     * 标记通知已读
     */
    @PutMapping("/{notificationId}/read")
    @ApiOperation("标记通知已读")
    public Result<Map<String, Object>> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        notificationService.markAsRead(notificationId, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notificationId);
        data.put("isRead", true);
        return Result.success(data, "标记已读成功");
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @ApiOperation("获取未读通知数量")
    public Result<Map<String, Object>> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);

        int count = notificationService.getUnreadCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", count);
        return Result.success(data);
    }
}
