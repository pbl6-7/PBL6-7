package com.campus.activity.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户统计DTO
 */
@Data
@Builder
public class UserStatisticsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long totalRegistrations;
    private Map<String, Long> roleDistribution;
    private List<TrendDataDTO> monthlyRegistrationTrend;
    private List<TrendDataDTO> weeklyRegistrationTrend;
    private Long inactiveUsers;
    private Map<String, Long> registrationDistribution;
    private Map<String, Long> monthlyTrendData;
    private Double averageRegistrationsPerUser;
    private Long newUsers7Days;
    private Long newUsers30Days;
}
