package com.campus.activity.service;

import com.campus.activity.dto.NotificationPageResponse;
import com.campus.activity.dto.NotificationResponse;
import com.campus.activity.mapper.NotificationMapper;
import com.campus.activity.entity.Notification;
import com.campus.core.util.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private com.campus.user.mapper.UserMapper userMapper;

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    @Override
    public void notifyUser(Long userId, String notificationType, String message) {
        notifyUser(userId, notificationType, null, message);
    }

    @Override
    public void notifyUser(Long userId, String notificationType, String title, String message) {
        log.info("=== notifyUser 开始 ===");
        log.info("userId: {}, notificationType: {}, title: {}, message: {}", userId, notificationType, title, message);
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setActivityId(0L); // 0 表示无关联活动
        notification.setType(notificationType);
        notification.setTitle(title != null ? title : notificationType);
        notification.setContent(message);
        notification.setCreateTime(LocalDateTime.now());
        notification.setIsRead(false);

        log.info("通知对象创建完成: {}", notification);
        
        try {
            int result = notificationMapper.insert(notification);
            log.info("通知插入结果: affectedRows={}, generatedId={}", result, notification.getId());

            // 通过WebSocket实时推送通知
            try {
                webSocketNotificationService.sendToUser(userId, notificationType, "系统通知", message);
            } catch (Exception wsEx) {
                log.warn("WebSocket推送失败，不影响数据库通知: {}", wsEx.getMessage());
            }
        } catch (Exception e) {
            log.error("通知插入失败: userId={}, type={}, error={}", userId, notificationType, e.getMessage(), e);
        }
        
        log.info("=== notifyUser 结束 ===");
    }

    @Override
    public void notifyUsers(List<Long> userIds, String notificationType, String message) {
        for (Long userId : userIds) {
            notifyUser(userId, notificationType, message);
        }
    }

    @Override
    public void notifyAllUsers(String notificationType, String message) {
        log.info("发送系统通知: type={}, message={}", notificationType, message);
        try {
            List<Long> allUserIds = userMapper.selectAllIds();
            for (Long userId : allUserIds) {
                notifyUser(userId, notificationType, "系统通知", message);
            }
            log.info("系统通知发送完成，共通知{}个用户", allUserIds.size());
        } catch (Exception e) {
            log.error("发送系统通知失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public NotificationPageResponse getUserNotifications(Long userId, int page, int size) {
        PageUtils.PageParams params = PageUtils.validateAndNormalize(page, size);
        List<Notification> notifications = notificationMapper.selectByUserId(userId, (long) params.getOffset(), (long) params.getSize());
        long total = notificationMapper.countByUserId(userId);
        
        // 转换为 NotificationResponse
        List<NotificationResponse> records = notifications.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getActivityId(),
                        n.getTitle(),
                        n.getType(),
                        n.getContent(),
                        n.getIsRead(),
                        n.getCreateTime()
                ))
                .collect(Collectors.toList());
        
        return new NotificationPageResponse(records, total, (total + size - 1) / size, (long) page);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        try {
            // 先验证通知是否属于当前用户
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                log.warn("通知不存在: notificationId={}", notificationId);
                return;
            }
            if (!notification.getUserId().equals(userId)) {
                log.warn("用户尝试标记非自己的通知: userId={}, notificationId={}, ownerUserId={}",
                        userId, notificationId, notification.getUserId());
                return;
            }
            notificationMapper.updateIsRead(notificationId);
            log.info("标记通知为已读: notificationId={}, userId={}", notificationId, userId);
        } catch (Exception e) {
            log.warn("标记通知已读失败: notificationId={}, userId={}, error={}", notificationId, userId, e.getMessage());
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        try {
            return notificationMapper.countUnreadByUserId(userId);
        } catch (Exception e) {
            log.warn("获取未读通知数量失败: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        try {
            notificationMapper.updateAllReadByUserId(userId);
            log.info("标记所有通知为已读: userId={}", userId);
        } catch (Exception e) {
            log.warn("标记所有通知已读失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                log.warn("通知不存在: notificationId={}", notificationId);
                return;
            }
            if (!notification.getUserId().equals(userId)) {
                log.warn("用户尝试删除非自己的通知: userId={}, notificationId={}", userId, notificationId);
                return;
            }
            notificationMapper.deleteById(notificationId);
            log.info("删除通知成功: notificationId={}, userId={}", notificationId, userId);
        } catch (Exception e) {
            log.error("删除通知失败: notificationId={}, userId={}, error={}", notificationId, userId, e.getMessage(), e);
        }
    }
}
