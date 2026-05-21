package com.campus.activity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TopicCreateRequest {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotBlank(message = "话题标题不能为空")
    private String title;
}
