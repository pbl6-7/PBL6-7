package com.campus.activity.dto;

import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 报名状态更新请求DTO
 * 用于更新报名状态时接收请求参数
 */
@Data
public class RegistrationStatusUpdateRequest {

    /**
     * 报名ID
     */
    @NotNull(message = "报名ID不能为空", groups = {UpdateGroup.class})
    @Min(value = 1, message = "报名ID必须大于0", groups = {UpdateGroup.class})
    private Long registrationId;

    /**
     * 报名状态
     */
    @NotNull(message = "状态不能为空", groups = {UpdateGroup.class})
    @Pattern(regexp = "^(confirmed|cancelled)$", message = "状态必须是confirmed或cancelled", groups = {UpdateGroup.class})
    private String status;
}
