package com.campus.activity.dto;

import lombok.Data;
import java.util.Map;

/**
 * 订阅响应DTO
 * 用于返回订阅操作的结果
 */
@Data
public class SubscriptionResponse {
    private Long activityId;
    private Boolean subscribed;
    private Integer subscriptionCount;

    public SubscriptionResponse(Long activityId, Boolean subscribed, Integer subscriptionCount) {
        this.activityId = activityId;
        this.subscribed = subscribed;
        this.subscriptionCount = subscriptionCount;
    }
}
