package com.campus.activity.dto;

import com.campus.activity.entity.ActivityType;
import lombok.Data;

@Data
public class ActivityTypeResponse {
    private Long id;
    private String name;

    public static ActivityTypeResponse fromEntity(ActivityType type) {
        ActivityTypeResponse response = new ActivityTypeResponse();
        response.setId(type.getId());
        response.setName(type.getName());
        return response;
    }
}
