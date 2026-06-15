package com.campus.activity.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报名统计DTO
 */
@Data
@Builder
public class RegistrationStatisticsDTO {
    private Long totalRegistrations;
    private Long pendingRegistrations;
    private Long approvedRegistrations;
    private Long rejectedRegistrations;
    private Map<String, Long> statusDistribution;
    private List<TrendDataDTO> monthlyTrend;
    private List<TrendDataDTO> weeklyTrend;
    private Map<Long, Long> hotActivityRegistrations;
    private Map<Long, Long> userRegistrationCount;
    private Map<String, Long> monthlyTrendData;
    private Double averageRegistrationsPerActivity;
    private Long registrations7Days;
    private Long registrations30Days;
    private Double confirmationRate;
}
