package com.campus.user.dto;

import com.campus.core.validation.annotation.StrongPassword;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码请求DTO
 * 用于用户修改密码时接收请求参数
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空", groups = {UpdateGroup.class})
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空", groups = {UpdateGroup.class})
    @Size(min = 8, max = 50, message = "密码长度必须在8-50位之间", groups = {UpdateGroup.class})
    @StrongPassword(message = "密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符", groups = {UpdateGroup.class})
    private String newPassword;
}
