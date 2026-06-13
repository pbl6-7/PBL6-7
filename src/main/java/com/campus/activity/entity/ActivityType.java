package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityType {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
