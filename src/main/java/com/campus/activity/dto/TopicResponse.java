package com.campus.activity.dto;

import com.campus.activity.entity.ActivityTopic;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TopicResponse {
    private Long id;
    private Long activityId;
    private String title;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TopicResponse fromEntity(ActivityTopic topic) {
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId());
        response.setActivityId(topic.getActivityId());
        response.setTitle(topic.getTitle());
        response.setCreatorId(topic.getCreatorId());
        response.setCreatedAt(topic.getCreatedAt());
        response.setUpdatedAt(topic.getUpdatedAt());
        return response;
    }
}
