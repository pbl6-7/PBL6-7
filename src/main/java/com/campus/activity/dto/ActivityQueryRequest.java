package com.campus.activity.dto;

import lombok.Data;

@Data
public class ActivityQueryRequest {
    private String keyword;
    private String status;
    private String approvalStatus;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}
