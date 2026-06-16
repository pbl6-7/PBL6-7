package com.campus.activity.service;

import com.campus.activity.mapper.ActivityMapper;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 统计异步服务
 * 提供异步统计计算能力，避免阻塞主线程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsAsyncService {

    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    /**
     * 异步获取活动统计
     */
    @Async
    public CompletableFuture<Map<String, Object>> getActivityStatisticsAsync() {
        log.info("异步获取活动统计");
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalActivities", activityMapper.countAll());
            stats.put("publishedActivities", activityMapper.countByApprovalStatus("approved"));
            stats.put("pendingActivities", activityMapper.countByApprovalStatus("pending"));
            stats.put("rejectedActivities", activityMapper.countByApprovalStatus("rejected"));
            return CompletableFuture.completedFuture(stats);
        } catch (Exception e) {
            log.error("异步获取活动统计失败: {}", e.getMessage());
            return CompletableFuture.completedFuture(new HashMap<>());
        }
    }

    /**
     * 异步获取用户统计
     */
    @Async
    public CompletableFuture<Map<String, Object>> getUserStatisticsAsync() {
        log.info("异步获取用户统计");
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userMapper.countAll());
            stats.put("activeUsers", userMapper.countByStatus("enabled"));
            stats.put("disabledUsers", userMapper.countByStatus("disabled"));
            return CompletableFuture.completedFuture(stats);
        } catch (Exception e) {
            log.error("异步获取用户统计失败: {}", e.getMessage());
            return CompletableFuture.completedFuture(new HashMap<>());
        }
    }

    /**
     * 刷新缓存
     */
    public void refreshCache() {
        log.info("刷新统计缓存");
    }
}
