package com.campus.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏详情响应DTO
 * 用于返回用户收藏活动的详细信息
 */
@Data
public class CollectDetailResponse {

    /**
     * 收藏记录ID
     */
    private Long collectId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动地点
     */
    private String activityLocation;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 收藏时间
     */
    private LocalDateTime collectTime;

    /**
     * 全参数构造函数
     */
    public CollectDetailResponse(Long collectId, Long activityId, String activityTitle,
                                 String activityLocation, LocalDateTime startTime, LocalDateTime collectTime) {
        this.collectId = collectId;
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.activityLocation = activityLocation;
        this.startTime = startTime;
        this.collectTime = collectTime;
    }
}
