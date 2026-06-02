package com.campus.activity.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class RegistrationRequest {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
}
