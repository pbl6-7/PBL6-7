package com.campus.user.dto;

import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 验证密保答案请求DTO
 * 用于验证用户密保答案时接收请求参数
 */
@Data
public class VerifySecurityRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = {UpdateGroup.class})
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间", groups = {UpdateGroup.class})
    private String username;

    /**
     * 密保答案
     */
    @NotBlank(message = "密保答案不能为空", groups = {UpdateGroup.class})
    @Size(max = 100, message = "密保答案不能超过100个字符", groups = {UpdateGroup.class})
    private String securityAnswer;
}
