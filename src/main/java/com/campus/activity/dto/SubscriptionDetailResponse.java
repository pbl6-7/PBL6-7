package com.campus.activity.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订阅详情响应DTO
 * 用于返回用户订阅的详细信息
 */
@Data
public class SubscriptionDetailResponse {
    private Long activityId;
    private String activityTitle;
    private String activityLocation;
    private LocalDateTime activityStartTime;
    private LocalDateTime createTime;

    public SubscriptionDetailResponse(Long activityId, String activityTitle,
                                      String activityLocation, LocalDateTime activityStartTime,
                                      LocalDateTime createTime) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.activityLocation = activityLocation;
        this.activityStartTime = activityStartTime;
        this.createTime = createTime;
    }
}
