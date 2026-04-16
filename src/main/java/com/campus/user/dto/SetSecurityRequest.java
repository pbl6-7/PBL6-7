package com.campus.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 设置密保请求DTO
 */
@Data
public class SetSecurityRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "请选择密保问题")
    private Integer securityQuestionId;

    @NotBlank(message = "密保答案不能为空")
    @Size(min = 2, max = 100, message = "密保答案长度必须在2-100位之间")
    private String securityAnswer;
}
