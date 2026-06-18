package com.campus.activity.controller;

import com.campus.activity.dto.NotificationPageResponse;
import com.campus.activity.entity.Notification;
import com.campus.activity.mapper.NotificationMapper;
import com.campus.activity.service.NotificationService;
import com.campus.activity.service.WebSocketNotificationService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 管理员-公告通知控制器
 * 提供系统公告发布和通知管理功能，所有接口需要管理员权限
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@Api(tags = "管理员-公告通知管理")
public class AdminAnnouncementController {

    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationMapper notificationMapper;

    /**
     * 验证管理员权限
     */
    private void validateAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String role = (String) request.getAttribute("currentUserRole");
        if (userId == null || role == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        if (!UserRoleConstants.ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    /**
     * 发布系统公告通知给所有用户
     * @param request HTTP请求（用于权限校验）
     * @param body 请求体，包含 title（标题）和 content（内容）
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("发布系统公告")
    public Result<String> publishAnnouncement(HttpServletRequest request, @RequestBody Map<String, String> body) {
        validateAdmin(request);

        String title = body.get("title");
        String content = body.get("content");

        if (title == null || title.trim().isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST, "公告标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST, "公告内容不能为空");
        }

        // 批量写入数据库通知 + WebSocket广播
        notificationService.notifyAllUsers("SYSTEM_ANNOUNCEMENT", title, content);

        return Result.success("公告发布成功");
    }

    /**
     * 获取已发送公告列表（去重，每条公告只显示一条记录）
     *
     * @param request HTTP请求对象
     * @param page 页码（默认1）
     * @param size 每页数量（默认20）
     * @return 去重后的公告分页数据
     */
    @GetMapping("/notifications")
    @ApiOperation("获取已发送公告列表（去重）")
    public Result<NotificationPageResponse> getAllNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        validateAdmin(request);
        // 使用去重查询，只返回每条公告的一条记录，而非所有用户收到的通知副本
        NotificationPageResponse notifications = notificationService.getDistinctAnnouncements("SYSTEM_ANNOUNCEMENT", page, size);
        return Result.success(notifications);
    }

    /**
     * 管理员删除公告（同时删除所有用户收到的该公告通知副本）
     *
     * @param request HTTP请求对象
     * @param id 公告通知ID（去重列表中的代表记录ID）
     * @return 操作结果
     */
    @DeleteMapping("/notifications/{id}")
    @ApiOperation("管理员删除公告")
    public Result<Void> deleteNotification(
            HttpServletRequest request,
            @PathVariable Long id) {
        validateAdmin(request);
        // 先查出该公告的标题和内容，然后删除所有匹配的通知（包括所有用户的副本）
        Notification notification = notificationMapper.selectById(id);
        if (notification != null) {
            notificationMapper.deleteByTitleAndContent(notification.getTitle(), notification.getContent());
            log.info("管理员删除公告: title={}, 影响所有用户副本", notification.getTitle());
        }
        return Result.success(null, "公告删除成功");
    }
}
