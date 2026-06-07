package com.campus.activity.service;

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
}