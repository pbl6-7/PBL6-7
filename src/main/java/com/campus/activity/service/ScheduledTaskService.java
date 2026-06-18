package com.campus.activity.service;

import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.activity.mapper.ActivitySubscriptionMapper;
import com.campus.activity.mapper.SearchHistoryMapper;
import com.campus.activity.mapper.NotificationMapper;
import com.campus.core.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 定时任务服务
 * 提供活动提醒、状态自动更新、数据清理等定时任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final ActivitySubscriptionMapper subscriptionMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final NotificationMapper notificationMapper;
    private final NotificationServiceImpl notificationService;
    private final AuditService auditService;
    private final CacheService cacheService;
    private final com.campus.core.mapper.AuditLogMapper auditLogMapper;
    private final com.campus.core.service.JwtKeyManager jwtKeyManager;

    /**
     * 活动开始前提醒
     * 每小时检查一次即将开始的活动（1小时内），通知已报名用户和订阅用户
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void remindUpcomingActivities() {
        log.info("定时任务：检查即将开始的活动");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);

            // 查询1小时内即将开始的活动
            List<Map<String, Object>> upcomingActivities = activityMapper.selectUpcomingActivities(now, oneHourLater);

            for (Map<String, Object> activity : upcomingActivities) {
                Long activityId = (Long) activity.get("id");
                String title = (String) activity.get("title");

                // 通知已报名用户
                List<Long> registeredUserIds = registrationMapper.selectRegisteredUserIdsByActivityId(activityId);
                for (Long userId : registeredUserIds) {
                    try {
                        notificationService.notifyUser(userId, "ACTIVITY_REMINDER",
                                "活动即将开始", "您报名的活动「" + title + "」即将在1小时内开始，请做好准备。");
                    } catch (Exception e) {
                        log.warn("发送活动提醒失败: userId={}, activityId={}, error={}", userId, activityId, e.getMessage());
                    }
                }

                // 通知订阅用户
                List<Long> subscribedUserIds = subscriptionMapper.selectUserIdsByActivityId(activityId);
                for (Long userId : subscribedUserIds) {
                    // 跳过已报名用户（避免重复通知）
                    if (registeredUserIds.contains(userId)) {
                        continue;
                    }
                    try {
                        notificationService.notifyUser(userId, "ACTIVITY_REMINDER",
                                "活动即将开始", "您订阅的活动「" + title + "」即将在1小时内开始。");
                    } catch (Exception e) {
                        log.warn("发送订阅活动提醒失败: userId={}, activityId={}, error={}", userId, activityId, e.getMessage());
                    }
                }
            }

            if (!upcomingActivities.isEmpty()) {
                log.info("已发送{}个活动的开始提醒", upcomingActivities.size());
            }
        } catch (Exception e) {
            log.error("活动提醒定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 活动状态自动更新
     * 每30分钟检查一次，将已过期的活动状态自动更新为ended，并通知订阅用户
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void autoUpdateActivityStatus() {
        log.info("定时任务：自动更新活动状态");
        try {
            LocalDateTime now = LocalDateTime.now();

            // 先查询即将过期的活动（用于发送通知）
            List<Map<String, Object>> expiredActivities = activityMapper.selectExpiredButActiveActivities(now);

            // 批量更新状态
            int updated = activityMapper.autoEndExpiredActivities(now);
            if (updated > 0) {
                log.info("自动结束{}个已过期活动", updated);
                cacheService.clearHotActivityCache();

                // 通知订阅用户活动已自动结束
                for (Map<String, Object> activity : expiredActivities) {
                    Long activityId = (Long) activity.get("id");
                    String title = (String) activity.get("title");
                    try {
                        List<Long> subscriberIds = subscriptionMapper.selectUserIdsByActivityId(activityId);
                        for (Long userId : subscriberIds) {
                            try {
                                notificationService.notifyUser(userId, "activity_ended", "活动已结束",
                                        "您订阅的活动「" + title + "」已结束，感谢您的关注。");
                            } catch (Exception e) {
                                log.warn("通知订阅用户活动结束失败: userId={}, activityId={}, error={}", userId, activityId, e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("获取订阅用户列表失败: activityId={}, error={}", activityId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("活动状态自动更新定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理过期搜索历史
     * 每天凌晨3点执行，删除30天前的搜索历史
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldSearchHistory() {
        log.info("定时任务：清理过期搜索历史");
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            searchHistoryMapper.deleteOldSearchHistory(cutoff);
            log.info("已清理30天前的搜索历史");
        } catch (Exception e) {
            log.error("清理搜索历史定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理过期通知
     * 每天凌晨4点执行，删除90天前的已读通知
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupOldNotifications() {
        log.info("定时任务：清理过期通知");
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
            notificationMapper.deleteOldNotifications(cutoff);
            log.info("已清理90天前的已读通知");
        } catch (Exception e) {
            log.error("清理通知定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理审计日志
     * 每天凌晨5点执行，保留最近180天的审计日志
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void cleanupOldAuditLogs() {
        log.info("定时任务：清理过期审计日志");
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(180);
            auditLogMapper.deleteOldAuditLogs(cutoff);
            log.info("已清理180天前的审计日志");
        } catch (Exception e) {
            log.error("清理审计日志定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 刷新统计缓存
     * 每小时刷新一次热门活动缓存
     */
    @Scheduled(cron = "0 30 * * * ?")
    public void refreshStatisticsCache() {
        log.info("定时任务：刷新统计缓存");
        try {
            cacheService.clearHotActivityCache();
            log.info("统计缓存刷新完成");
        } catch (Exception e) {
            log.error("刷新统计缓存定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * JWT密钥自动轮换检查
     * 每天凌晨2点检查密钥是否需要轮换
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkJwtKeyRotation() {
        log.info("定时任务：检查JWT密钥轮换");
        try {
            jwtKeyManager.checkAndRotateIfNeeded();
            log.info("JWT密钥轮换检查完成");
        } catch (Exception e) {
            log.error("JWT密钥轮换检查失败: {}", e.getMessage(), e);
        }
    }
}
