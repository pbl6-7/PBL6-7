package com.campus.user.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    private String contact;
}
