package com.campus.activity.dto;

import com.campus.activity.entity.ActivityTag;
import lombok.Data;

@Data
public class ActivityTagRelationDTO {
    private Long activityId;
    private Long id;
    private String name;
    private String color;

    public TagResponse toTagResponse() {
        TagResponse response = new TagResponse();
        response.setId(this.id);
        response.setName(this.name);
        response.setColor(this.color);
        return response;
    }
}
