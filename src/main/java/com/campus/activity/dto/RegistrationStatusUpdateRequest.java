package com.campus.activity.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class RegistrationStatusUpdateRequest {
    @NotNull(message = "报名ID不能为空")
    private Long registrationId;

    @NotNull(message = "状态不能为空")
    @Pattern(regexp = "^(confirmed|cancelled)$", message = "状态必须是confirmed或cancelled")
    private String status;
}
