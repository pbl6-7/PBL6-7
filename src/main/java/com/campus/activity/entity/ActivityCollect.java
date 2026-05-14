package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityCollect {
    private Long id;
    private Long userId;
    private Long activityId;
    private LocalDateTime createTime;
}
