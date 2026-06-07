package com.campus.user.entity;

import com.campus.core.validation.annotation.PhoneNumber;
import com.campus.core.validation.annotation.StrongPassword;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于存储用户基本信息
 */
@Data
public class User {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = {CreateGroup.class})
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线", groups = {CreateGroup.class, UpdateGroup.class})
    private String username;

    /**
     * 密码（加密存储）
     */
    @NotBlank(message = "密码不能为空", groups = {CreateGroup.class})
    @StrongPassword(message = "密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符", groups = {CreateGroup.class})
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空", groups = {CreateGroup.class})
    @Size(max = 50, message = "真实姓名不能超过50个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String realName;

    /**
     * 用户角色
     */
    @Pattern(regexp = "^(USER|ADMIN)$", message = "用户角色只能是USER或ADMIN", groups = {CreateGroup.class, UpdateGroup.class})
    private String role;

    /**
     * 用户状态（enabled-启用，disabled-禁用）
     */
    private String status;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 联系方式（手机号）
     */
    @PhoneNumber(message = "手机号格式不正确，应为11位数字且以1开头", groups = {CreateGroup.class, UpdateGroup.class})
    private String contact;

    /**
     * 密保问题ID
     */
    @Min(value = 1, message = "密保问题ID必须大于0", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer securityQuestionId;

    /**
     * 密保答案
     */
    @Size(max = 100, message = "密保答案不能超过100个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String securityAnswer;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}
