package com.campus.activity.service;

import com.campus.activity.dto.NotificationPageResponse;
import com.campus.activity.dto.NotificationResponse;
import com.campus.activity.mapper.NotificationMapper;
import com.campus.activity.entity.Notification;
import com.campus.core.util.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        log.debug("发送通知: userId={}, type={}, title={}", userId, notificationType, title);
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setActivityId(0L); // 0 表示无关联活动
        notification.setType(notificationType);
        notification.setTitle(title != null ? title : notificationType);
        notification.setContent(message);
        notification.setCreateTime(LocalDateTime.now());
        notification.setIsRead(false);

        try {
            int result = notificationMapper.insert(notification);
            log.debug("通知插入成功: id={}", notification.getId());

            // 通过WebSocket实时推送通知（包含通知ID）
            try {
                webSocketNotificationService.sendToUser(userId, notification.getId(), notificationType, title != null ? title : notificationType, message);
            } catch (Exception wsEx) {
                log.warn("WebSocket推送失败，不影响数据库通知: {}", wsEx.getMessage());
            }
        } catch (Exception e) {
            log.error("通知插入失败: userId={}, type={}, error={}", userId, notificationType, e.getMessage(), e);
            throw new RuntimeException("通知插入失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void notifyUsers(List<Long> userIds, String notificationType, String message) {
        for (Long userId : userIds) {
            notifyUser(userId, notificationType, message);
        }
    }

    @Override
    public void notifyAllUsers(String notificationType, String message) {
        notifyAllUsers(notificationType, "系统通知", message);
    }

    /**
     * 发送系统通知给所有用户（带标题）
     * 使用批量插入优化性能，每批500条，并通过WebSocket广播
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyAllUsers(String notificationType, String title, String message) {
        log.info("发送系统公告通知: type={}, title={}", notificationType, title);
        try {
            List<Long> allUserIds = userMapper.selectAllIds();
            if (allUserIds.isEmpty()) {
                log.info("没有用户需要通知");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            int batchSize = 500;
            int totalInserted = 0;

            // 分批构建通知对象并批量插入
            for (int i = 0; i < allUserIds.size(); i += batchSize) {
                List<Long> batchIds = allUserIds.subList(i, Math.min(i + batchSize, allUserIds.size()));
                List<Notification> batchNotifications = new ArrayList<>(batchIds.size());

                for (Long userId : batchIds) {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setActivityId(0L);
                    notification.setType(notificationType);
                    notification.setTitle(title);
                    notification.setContent(message);
                    notification.setIsRead(false);
                    notification.setCreateTime(now);
                    batchNotifications.add(notification);
                }

                notificationMapper.batchInsert(batchNotifications);
                totalInserted += batchNotifications.size();
            }

            // 通过WebSocket广播通知
            webSocketNotificationService.broadcast(notificationType, title, message);

            log.info("系统公告通知发送完成，共通知{}个用户", totalInserted);
        } catch (Exception e) {
            log.error("发送系统公告通知失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送系统公告通知失败: " + e.getMessage(), e);
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

    /**
     * 获取所有通知列表（管理员用，分页）
     *
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页响应
     */
    @Override
    public NotificationPageResponse getAllNotifications(int page, int size) {
        int offset = (page - 1) * size;
        List<Notification> notifications = notificationMapper.selectAllRecent(offset, size);
        Long total = notificationMapper.countAll();
        Long totalPages = (long) Math.ceil((double) total / size);

        // 将 Notification 实体转换为 NotificationResponse DTO
        List<com.campus.activity.dto.NotificationResponse> responses = notifications.stream()
                .map(n -> new com.campus.activity.dto.NotificationResponse(
                        n.getId(), n.getActivityId(), n.getTitle(), n.getType(),
                        n.getContent(), n.getIsRead(), n.getCreateTime()))
                .collect(java.util.stream.Collectors.toList());

        return new NotificationPageResponse(responses, total, totalPages, (long) page);
    }

    /**
     * 获取去重后的系统公告列表
     * 按title+content分组，每条公告只显示一条记录
     */
    @Override
    public NotificationPageResponse getDistinctAnnouncements(String type, int page, int size) {
        int offset = (page - 1) * size;
        List<Notification> notifications = notificationMapper.selectDistinctByType(type, offset, size);
        Long total = notificationMapper.countDistinctByType(type);
        Long totalPages = (long) Math.ceil((double) total / size);

        List<com.campus.activity.dto.NotificationResponse> responses = notifications.stream()
                .map(n -> new com.campus.activity.dto.NotificationResponse(
                        n.getId(), n.getActivityId(), n.getTitle(), n.getType(),
                        n.getContent(), n.getIsRead(), n.getCreateTime()))
                .collect(java.util.stream.Collectors.toList());

        return new NotificationPageResponse(responses, total, totalPages, (long) page);
    }
}
