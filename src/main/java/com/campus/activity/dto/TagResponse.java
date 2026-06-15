package com.campus.activity.dto;

import com.campus.activity.entity.ActivityTag;
import com.campus.activity.entity.Tag;
import lombok.Data;

@Data
public class TagResponse {
    private Long id;
    private Long activityId;
    private String name;
    private String color;

    public static TagResponse fromEntity(ActivityTag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setActivityId(tag.getActivityId());
        response.setName(tag.getName());
        response.setColor(tag.getColor());
        return response;
    }

    public static TagResponse fromTagEntity(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setActivityId(null);
        response.setName(tag.getName());
        response.setColor(tag.getColor());
        return response;
    }
}
