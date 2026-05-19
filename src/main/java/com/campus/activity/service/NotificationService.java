package com.campus.activity.service;

import com.campus.activity.dto.NotificationPageResponse;
import com.campus.activity.dto.NotificationResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Notification;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.NotificationMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务层
 * 提供通知相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final ActivityMapper activityMapper;
    private final ActivitySubscriptionService subscriptionService;

    public static final String TYPE_SUBSCRIPTION_STATUS = "SUBSCRIPTION_STATUS";
    public static final String TYPE_ACTIVITY_UPDATE = "ACTIVITY_UPDATE";
    public static final String TYPE_ACTIVITY_START = "ACTIVITY_START";
    public static final String TYPE_ACTIVITY_END = "ACTIVITY_END";

    /**
     * 创建通知
     * @param userId 用户ID
     * @param activityId 活动ID
     * @param type 通知类型
     * @param content 通知内容
     */
    @Transactional
    public void createNotification(Long userId, Long activityId, String type, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setActivityId(activityId);
        notification.setType(type);
        notification.setContent(content);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
    }

    /**
     * 分页获取用户通知
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页通知列表
     */
    public NotificationPageResponse getUserNotifications(Long userId, int page, int size) {
        long offset = (long) (page - 1) * size;
        List<Notification> notifications = notificationMapper.selectByUserId(userId, offset, (long) size);
        int total = notificationMapper.countByUserId(userId);

        List<NotificationResponse> responses = new ArrayList<>();
        for (Notification notification : notifications) {
            Activity activity = activityMapper.selectById(notification.getActivityId());
            String activityTitle = activity != null ? activity.getTitle() : "未知活动";
            NotificationResponse response = new NotificationResponse(
                    notification.getId(),
                    notification.getActivityId(),
                    activityTitle,
                    notification.getType(),
                    notification.getContent(),
                    notification.getIsRead(),
                    notification.getCreateTime()
            );
            responses.add(response);
        }

        long pages = (total + size - 1) / size;
        return new NotificationPageResponse(responses, (long) total, pages, (long) page);
    }

    /**
     * 标记通知已读
     * @param notificationId 通知ID
     * @param userId 用户ID（用于权限验证）
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此通知");
        }

        notificationMapper.updateIsRead(notificationId);
    }

    /**
     * 获取未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 通知活动的所有订阅者
     * @param activityId 活动ID
     * @param type 通知类型
     * @param content 通知内容
     */
    @Transactional
    public void notifySubscribers(Long activityId, String type, String content) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return;
        }

        List<Long> subscriberIds = subscriptionService.getSubscribedUserIds(activityId);
        for (Long userId : subscriberIds) {
            createNotification(userId, activityId, type, content);
        }
    }

    /**
     * 通知活动的所有订阅者（带活动标题）
     * @param activityId 活动ID
     * @param type 通知类型
     * @param content 通知内容
     */
    @Transactional
    public void notifySubscribersWithTitle(Long activityId, String type, String content) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return;
        }

        String fullContent = "【" + activity.getTitle() + "】" + content;
        notifySubscribers(activityId, type, fullContent);
    }
}
