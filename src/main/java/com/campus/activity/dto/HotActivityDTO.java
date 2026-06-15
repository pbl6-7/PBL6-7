package com.campus.activity.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 热门活动DTO
 */
@Data
@Builder
public class HotActivityDTO {
    private Long activityId;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private Long viewCount;
    private Long registrationCount;
    private Double hotScore;
    private String activityType;
    private Long collectionCount;
    private String status;
    private Double popularityScore;
}
