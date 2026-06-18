package com.campus.activity.service;

import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.user.dto.UserResponse;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMonitorService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final UserMapper userMapper;
    private final CacheService cacheService;

    private Map<Long, User> batchGetUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    public Map<String, Object> getSystemStatus() {
        Long totalActivities = activityMapper.countAll();
        Long pendingActivities = activityMapper.countByApprovalStatus("pending");
        Long approvedActivities = activityMapper.countByApprovalStatus("approved");
        Long totalRegistrations = registrationMapper.countAll();
        Long totalUsers = userMapper.countAllUsers();

        return Map.of(
            "totalActivities", totalActivities,
            "pendingActivities", pendingActivities,
            "approvedActivities", approvedActivities,
            "totalRegistrations", totalRegistrations,
            "totalUsers", totalUsers,
            "status", "running"
        );
    }

    public Map<String, Object> getSystemMetrics() {
        Long activities7Days = activityMapper.countCreatedAfterDays(7);
        Long registrations7Days = registrationMapper.countRecentRegistrations(7);
        Long recentUsers = userMapper.countRecentUsers(7);

        return Map.of(
            "activitiesLast7Days", activities7Days,
            "registrationsLast7Days", registrations7Days,
            "newUsersLast7Days", recentUsers
        );
    }

    public List<ActivityResponse> getRecentActivities() {
        List<Activity> activities = activityMapper.selectRecentActivities(10);

        Set<Long> userIds = activities.stream()
                .map(Activity::getPublisherId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = batchGetUsers(userIds);

        List<ActivityResponse> responses = activities.stream()
                .map(ActivityResponse::fromEntity)
                .collect(Collectors.toList());

        responses.forEach(response -> {
            if (response.getPublisherId() != null && userMap.containsKey(response.getPublisherId())) {
                response.setPublisherName(userMap.get(response.getPublisherId()).getRealName());
            }
        });

        return responses;
    }

    public List<UserResponse> getRecentUsers() {
        List<User> users = userMapper.selectRecentUsers(10);

        return users.stream()
            .map(UserResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * 获取缓存信息
     */
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("cacheSize", cacheService.size());
        info.put("maxCacheSize", 1000);
        info.put("usagePercent", String.format("%.1f%%", cacheService.size() * 100.0 / 1000));
        return info;
    }
}
