package com.campus.activity.dto;

import lombok.Data;
import java.util.List;

@Data
public class RegistrationPageResponse {
    private List<RegistrationResponse> records;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;
}
