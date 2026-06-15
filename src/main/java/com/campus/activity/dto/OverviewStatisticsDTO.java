package com.campus.activity.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 概览统计DTO
 */
@Data
@Builder
public class OverviewStatisticsDTO {
    private Long totalActivities;
    private Long totalUsers;
    private Long totalRegistrations;
    private Long todayActivities;
    private Long todayRegistrations;
    private Long newActivities7Days;
    private Long newRegistrations7Days;
    private Long newUsers7Days;
    private Long pendingActivities;
    private Long newActivities30Days;
    private Long newUsers30Days;
    private Long newRegistrations30Days;
    private Long activeUsers;
    private Double systemHealthScore;
    private String updateTime;
}
