package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动订阅实体类
 * 用于存储用户对活动的订阅关系
 */
@Data
public class ActivitySubscription {
    private Long id;
    private Long userId;
    private Long activityId;
    private LocalDateTime createTime;
}
