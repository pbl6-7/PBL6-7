package com.campus.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityApprovalStatistics {
    private Long total;
    private Long pending;
    private Long approved;
    private Long rejected;
    private List<ActivityResponse> pendingActivities;
    private Map<String, Long> dailyStatistics;
}
