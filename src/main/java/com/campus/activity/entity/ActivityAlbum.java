package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityAlbum {
    private Long id;
    private Long activityId;
    private String url;
    private String thumbnailUrl;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private Long createdBy;
}
