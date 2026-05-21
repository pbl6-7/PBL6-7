package com.campus.activity.service;

import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final UserMapper userMapper;

    public Map<String, Object> getActivityStatistics() {
        Long total = activityMapper.countAll();
        Long pending = activityMapper.countByApprovalStatus("pending");
        Long approved = activityMapper.countByApprovalStatus("approved");
        Long rejected = activityMapper.countByApprovalStatus("rejected");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("pending", pending);
        statistics.put("approved", approved);
        statistics.put("rejected", rejected);

        return statistics;
    }

    public Map<String, Object> getUserStatistics() {
        Long totalUsers = (long) userMapper.selectAllUsers().size();
        Long publishers = (long) userMapper.selectUsersByRole("publisher").size();
        Long admins = (long) userMapper.selectUsersByRole("admin").size();
        Long regularUsers = (long) userMapper.selectUsersByRole("user").size();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("publishers", publishers);
        statistics.put("admins", admins);
        statistics.put("regularUsers", regularUsers);

        return statistics;
    }

    public Map<String, Object> getRegistrationStatistics() {
        Long totalRegistrations = registrationMapper.countAll();
        Long registrations7Days = registrationMapper.countRecentRegistrations(7);
        Long registrations30Days = registrationMapper.countRecentRegistrations(30);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRegistrations", totalRegistrations);
        statistics.put("registrations7Days", registrations7Days);
        statistics.put("registrations30Days", registrations30Days);

        return statistics;
    }

    public Map<String, Object> getDailyStatistics() {
        Long activities7Days = activityMapper.countAll();
        Long registrations7Days = registrationMapper.countRecentRegistrations(7);
        Long newUsers7Days = (long) userMapper.selectAllUsers().stream()
            .filter(u -> u.getCreatedAt() != null &&
                u.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(7)))
            .count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("activitiesThisWeek", activities7Days);
        statistics.put("registrationsThisWeek", registrations7Days);
        statistics.put("newUsersThisWeek", newUsers7Days);

        return statistics;
    }
}
