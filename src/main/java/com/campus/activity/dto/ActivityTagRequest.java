package com.campus.activity.dto;

import lombok.Data;
import java.util.List;

/**
 * 活动标签请求DTO
 */
@Data
public class ActivityTagRequest {
    private List<String> tagNames;
    private List<Long> tagIds;
    private Long activityId;
}
