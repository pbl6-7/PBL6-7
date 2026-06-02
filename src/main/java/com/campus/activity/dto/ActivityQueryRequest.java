package com.campus.activity.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityQueryRequest {
    private String keyword;
    private String status;
    private String approvalStatus;
    private String activityType;
    private String location;
    private LocalDateTime startTimeFrom;
    private LocalDateTime startTimeTo;
    private Long tagId;
    private Integer minParticipants;
    private Integer maxParticipants;
    private Long tagId;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}
