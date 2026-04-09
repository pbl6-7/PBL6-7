package com.campus.user.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Long userId, String username, String realName, String role, String token) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.token = token;
    }
}
