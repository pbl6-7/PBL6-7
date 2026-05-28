package com.campus.activity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ActivityTypeCreateRequest {
    @NotBlank(message = "类型名称不能为空")
    private String name;
}
