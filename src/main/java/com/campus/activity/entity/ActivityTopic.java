package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityTopic {
    private Long id;
    private Long activityId;
    private String title;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
