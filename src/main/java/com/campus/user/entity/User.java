package com.campus.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    /**
     * 真实姓名
     */
    private String realName;
    private String role;
    /**
     * 联系方式
     */
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
