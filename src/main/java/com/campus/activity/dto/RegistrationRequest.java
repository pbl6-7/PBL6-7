package com.campus.activity.dto;

import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 活动报名请求DTO
 * 用于用户报名参加活动时接收请求参数
 */
@Data
public class RegistrationRequest {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空", groups = {CreateGroup.class})
    @Min(value = 1, message = "活动ID必须大于0", groups = {CreateGroup.class})
    private Long activityId;
}
