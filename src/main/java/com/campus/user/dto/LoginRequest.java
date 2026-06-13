package com.campus.user.dto;

import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求DTO
 * 用于用户登录时接收请求参数
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 8, max = 50, message = "密码长度必须在8-50个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String password;
}
