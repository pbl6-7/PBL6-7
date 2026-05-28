package com.campus.activity.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ActivityTagRequest {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    private List<Long> tagIds;
}
