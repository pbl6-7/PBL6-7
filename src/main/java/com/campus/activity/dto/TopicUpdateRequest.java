package com.campus.activity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class TopicUpdateRequest {
    @NotBlank(message = "话题标题不能为空")
    private String title;
}
