package com.campus.user.dto;

import com.campus.user.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String contact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setContact(user.getContact());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
