package com.campus.activity.dto;

import lombok.Data;
import java.util.List;

@Data
public class ActivityPageResponse {
    private List<ActivityResponse> records;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;
}
