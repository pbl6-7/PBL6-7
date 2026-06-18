package com.campus.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket通知推送服务
 * 通过STOMP协议向客户端实时推送通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 向指定用户推送通知
     * @param userId 用户ID
     * @param notificationId 通知ID
     * @param type 通知类型
     * @param title 通知标题
     * @param message 通知内容
     */
    public void sendToUser(Long userId, Long notificationId, String type, String title, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", notificationId);
            notification.put("type", type);
            notification.put("title", title);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
            log.debug("WebSocket推送通知给用户 {}: {}", userId, title);
        } catch (Exception e) {
            log.warn("WebSocket推送通知失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 向指定用户推送通知（兼容旧调用方式）
     * @param userId 用户ID
     * @param type 通知类型
     * @param title 通知标题
     * @param message 通知内容
     */
    public void sendToUser(Long userId, String type, String title, String message) {
        sendToUser(userId, null, type, title, message);
    }

    /**
     * 向所有用户广播通知
     * @param type 通知类型
     * @param title 通知标题
     * @param message 通知内容
     */
    public void broadcast(String type, String title, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("title", title);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("WebSocket广播通知: {}", title);
        } catch (Exception e) {
            log.warn("WebSocket广播通知失败: error={}", e.getMessage());
        }
    }

    /**
     * 向订阅特定活动的用户推送活动更新
     * @param activityId 活动ID
     * @param type 通知类型
     * @param message 通知内容
     */
    public void sendToActivitySubscribers(Long activityId, String type, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("activityId", activityId);
            notification.put("type", type);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/activity/" + activityId, notification);
            log.debug("WebSocket推送活动通知: activityId={}, type={}", activityId, type);
        } catch (Exception e) {
            log.warn("WebSocket推送活动通知失败: activityId={}, error={}", activityId, e.getMessage());
        }
    }
}
