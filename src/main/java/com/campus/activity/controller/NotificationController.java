package com.campus.activity.controller;

import com.campus.activity.dto.NotificationPageResponse;
import com.campus.activity.service.NotificationService;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器
 * 提供通知相关的API接口
 */
@Api(tags = "通知管理")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前用户通知列表
     */
    @GetMapping
    @ApiOperation("获取当前用户通知列表")
    public Result<NotificationPageResponse> getMyNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 查询通知列表", userId);

        // 分页参数验证
        if (page < 1) {
            return Result.error(ResultCode.VALIDATION_ERROR, "页码必须大于0");
        }
        if (size < 1 || size > 100) {
            return Result.error(ResultCode.VALIDATION_ERROR, "每页大小必须在1-100之间");
        }

        NotificationPageResponse notifications = notificationService.getUserNotifications(userId, page, size);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @ApiOperation("获取未读通知数量")
    public Result<Map<String, Object>> getUnreadCount(
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        int count = notificationService.getUnreadCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", count);
        return Result.success(data);
    }

    /**
     * 标记通知已读
     */
    @PatchMapping("/{id}/read")
    @ApiOperation("标记通知已读")
    public Result<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 标记通知 {} 为已读", userId, id);

        notificationService.markAsRead(id, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("isRead", true);
        return Result.success(data, "标记已读成功");
    }

    /**
     * 标记全部通知已读
     */
    @PatchMapping("/read-all")
    @ApiOperation("标记全部通知已读")
    public Result<Map<String, Object>> markAllAsRead(
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 标记全部通知为已读", userId);

        notificationService.markAllAsRead(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("isRead", true);
        return Result.success(data, "标记全部已读成功");
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除通知")
    public Result<Map<String, Object>> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        log.info("用户 {} 删除通知 {}", userId, id);

        notificationService.deleteNotification(id, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("deleted", true);
        return Result.success(data, "删除成功");
    }
}
