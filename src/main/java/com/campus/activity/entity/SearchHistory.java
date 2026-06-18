package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 搜索历史实体类
 * 用于记录用户搜索历史
 */
@Data
public class SearchHistory {

    /**
     * 搜索记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 搜索关键词
     */
    private String searchKeyword;

    /**
     * 搜索类型（activity-活动，user-用户，all-全部）
     */
    private String searchType;

    /**
     * 搜索时间
     */
    private LocalDateTime searchTime;
}
