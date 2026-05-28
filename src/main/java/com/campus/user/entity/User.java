package com.campus.user.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;
    private String role;
    /**
     * 联系方式
     */
    @Size(max = 100, message = "联系方式不能超过100个字符")
    private String contact;
    /**
     * 密保问题ID
     */
    private Integer securityQuestionId;
    /**
     * 密保答案
     */
    private String securityAnswer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
