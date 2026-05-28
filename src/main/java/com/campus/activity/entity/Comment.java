package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long activityId;
    private Long userId;
    private String content;
    private Long replyToId;
    private LocalDateTime createdAt;
}
