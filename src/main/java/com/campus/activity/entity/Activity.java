package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Activity {
    private Long id;
    private String title;
    private Long publisherId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String description;
    private String status;
    private String approvalStatus;
    private Integer maxParticipants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
