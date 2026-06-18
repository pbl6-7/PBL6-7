package com.campus.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动收藏实体类
 * 对应数据库表 activity_favorite
 */
@Data
public class ActivityFavorite {
    /**
     * 收藏记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
}
