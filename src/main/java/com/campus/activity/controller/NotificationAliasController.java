package com.campus.activity.controller;

import com.campus.activity.service.NotificationService;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知接口兼容控制器
 * 提供 /notification 单数形式的路径别名，与 /notifications 复数形式兼容
 */
@Api(tags = "通知管理（兼容路径）")
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationAliasController {

    private final NotificationService notificationService;

    /**
     * 获取用户通知列表（兼容路径）
     */
    @GetMapping("/my")
    public Result<?> getMyNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        return Result.success(notificationService.getUserNotifications(userId, page, size));
    }

    /**
     * 标记通知已读（兼容路径，使用PUT方法）
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        notificationService.markAsRead(id, userId);
        return Result.success(null, "标记已读成功");
    }

    /**
     * 获取未读通知数量（兼容路径）
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        int count = notificationService.getUnreadCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", count);
        return Result.success(data);
    }
}
