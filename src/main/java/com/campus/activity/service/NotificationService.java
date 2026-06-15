package com.campus.activity.service;

import com.campus.activity.dto.NotificationPageResponse;

/**
 * 通知服务接口
 * 提供通知发送功能
 */
public interface NotificationService {

    /**
     * 发送通知给指定用户
     * 
     * @param userId 用户ID
     * @param notificationType 通知类型
     * @param message 通知消息
     */
    void notifyUser(Long userId, String notificationType, String message);

    /**
     * 发送通知给指定用户（带标题）
     * 
     * @param userId 用户ID
     * @param notificationType 通知类型
     * @param title 通知标题
     * @param message 通知消息
     */
    void notifyUser(Long userId, String notificationType, String title, String message);

    /**
     * 批量发送通知
     * 
     * @param userIds 用户ID列表
     * @param notificationType 通知类型
     * @param message 通知消息
     */
    void notifyUsers(java.util.List<Long> userIds, String notificationType, String message);

    /**
     * 发送系统通知给所有用户
     * 
     * @param notificationType 通知类型
     * @param message 通知消息
     */
    void notifyAllUsers(String notificationType, String message);

    /**
     * 获取用户通知列表（分页）
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页响应
     */
    NotificationPageResponse getUserNotifications(Long userId, int page, int size);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);
}