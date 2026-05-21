package com.campus.activity.dto;

import com.campus.activity.entity.ActivityPhoto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityPhotoResponse {
    private Long id;
    private Long activityId;
    private String photoUrl;
    private String photoName;
    private Long uploadedBy;
    private LocalDateTime createdAt;

    public static ActivityPhotoResponse fromEntity(ActivityPhoto photo) {
        ActivityPhotoResponse response = new ActivityPhotoResponse();
        response.setId(photo.getId());
        response.setActivityId(photo.getActivityId());
        response.setPhotoUrl(photo.getPhotoUrl());
        response.setPhotoName(photo.getPhotoName());
        response.setUploadedBy(photo.getUploadedBy());
        response.setCreatedAt(photo.getCreatedAt());
        return response;
    }
}