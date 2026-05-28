package com.campus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {
    private List<UserResponse> list;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;
}
