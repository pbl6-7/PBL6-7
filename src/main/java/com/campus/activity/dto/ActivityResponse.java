package com.campus.activity.dto;

import com.campus.activity.entity.Activity;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityResponse {
    private Long id;
    private String title;
    private Long publisherId;
    private String publisherName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String description;
    private String status;
    private String approvalStatus;
    private Long typeId;
    private String activityTypeName;
    private Integer maxParticipants;
    private Integer viewCount;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ActivityResponse fromEntity(Activity activity) {
        if (activity == null) {
            return null;
        }

        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setTitle(activity.getTitle());
        response.setPublisherId(activity.getPublisherId());
        response.setStartTime(activity.getStartTime());
        response.setEndTime(activity.getEndTime());
        response.setLocation(activity.getLocation());
        response.setDescription(activity.getDescription());
        response.setStatus(activity.getStatus());
        response.setApprovalStatus(activity.getApprovalStatus());
        response.setTypeId(activity.getTypeId());
        response.setMaxParticipants(activity.getMaxParticipants());
        response.setViewCount(activity.getViewCount());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }
}
