package com.campus.activity.dto;

import com.campus.activity.entity.ActivityTag;
import lombok.Data;

@Data
public class TagResponse {
    private Long id;
    private String name;
    private String color;

    public static TagResponse fromEntity(ActivityTag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setColor(tag.getColor());
        return response;
    }
}
