package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityRegistration {
    private Long id;
    private Long activityId;
    private Long userId;
    private LocalDateTime registrationTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
