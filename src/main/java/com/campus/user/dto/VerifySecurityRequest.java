package com.campus.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 验证密保答案请求DTO
 */
@Data
public class VerifySecurityRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密保答案不能为空")
    private String securityAnswer;
}
