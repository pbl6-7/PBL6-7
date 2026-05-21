package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityTagRelation {
    private Long id;
    private Long activityId;
    private Long tagId;
    private LocalDateTime createdAt;
}
