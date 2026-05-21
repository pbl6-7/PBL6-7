package com.campus.activity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class TagCreateRequest {
    @NotBlank(message = "标签名称不能为空")
    private String name;

    private String color;
}
