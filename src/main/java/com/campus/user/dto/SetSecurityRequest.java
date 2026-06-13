package com.campus.user.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 设置密保请求DTO
 * userId从JWT获取，不需要前端传递
 */
@Data
public class SetSecurityRequest {

    /**
     * 当前登录密码（验证身份）
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 密保问题ID
     */
    @NotNull(message = "请选择密保问题")
    @Min(value = 1, message = "密保问题ID必须大于0")
    private Integer securityQuestionId;

    /**
     * 密保答案
     */
    @NotBlank(message = "密保答案不能为空")
    @Size(min = 2, max = 100, message = "密保答案长度必须在2-100个字符之间")
    private String securityAnswer;
}
