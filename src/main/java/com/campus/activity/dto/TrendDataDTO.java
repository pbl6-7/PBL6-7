package com.campus.activity.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 趋势数据DTO
 */
@Data
@Builder
public class TrendDataDTO {
    private String date;
    private Long value;
    private Double changeRate;
    private String label;
    private Double growthRate;
    private Long cumulativeValue;
    private Long secondaryValue;
}
