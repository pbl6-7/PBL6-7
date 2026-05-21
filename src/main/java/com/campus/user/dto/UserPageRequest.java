package com.campus.user.dto;

import lombok.Data;

@Data
public class UserPageRequest {
    private String keyword;
    private String role;
    private Integer page;
    private Integer size;
}
