package com.campus.user.dto;

import com.campus.core.validation.annotation.StrongPassword;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 重置密码请求DTO
 * 用于用户通过密保问题重置密码时接收请求参数
 */
@Data
public class ResetPasswordRequest {

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

    /**
     * 密保验证Token
     * 验证密保答案后获取的一次性Token，用于关联验证和重置流程
     */
    @NotBlank(message = "密保验证Token不能为空")
    private String verifyToken;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空", groups = {UpdateGroup.class})
    @Size(min = 8, max = 50, message = "密码长度必须在8-50位之间", groups = {UpdateGroup.class})
    @StrongPassword(message = "密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符", groups = {UpdateGroup.class})
    private String newPassword;
}
