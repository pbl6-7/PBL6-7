package com.campus.activity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 搜索建议DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchSuggestion {
    private String keyword;
    private int relevanceScore;
    private Long searchCount;
    private String type;
}
