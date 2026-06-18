package com.campus.activity.dto;

import lombok.Data;

@Data
public class AlbumUploadRequest {
    private Long activityId;
    private String description;
    private Integer sortOrder;
}
