package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityPhoto {
    private Long id;
    private Long activityId;
    private String photoUrl;
    private String photoName;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}