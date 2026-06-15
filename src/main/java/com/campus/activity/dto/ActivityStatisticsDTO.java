package com.campus.activity.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 活动统计DTO
 */
@Data
@Builder
public class ActivityStatisticsDTO {
    private Long totalActivities;
    private Long publishedActivities;
    private Long draftActivities;
    private Long cancelledActivities;
    private Long totalViews;
    private Long totalParticipants;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> approvalStatusDistribution;
    private Map<String, Long> typeDistribution;
    private List<TrendDataDTO> monthlyTrend;
    private List<TrendDataDTO> weeklyTrend;
    private List<HotActivityDTO> hotActivitiesByRegistration;
    private List<HotActivityDTO> hotActivitiesByView;
    private List<HotActivityDTO> hotActivitiesByCollection;
    private Double averageRegistrations;
    private Double averageViewCount;
}
