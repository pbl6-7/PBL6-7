package com.campus.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户密保信息实体类
 */
@Data
public class UserSecurity {
    private Long id;
    private Long userId;
    private Integer securityQuestionId;
    private String securityAnswer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
