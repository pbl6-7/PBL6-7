package com.campus.activity.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页响应DTO
 * 用于返回分页的通知列表
 */
@Data
public class NotificationPageResponse {
    private List<NotificationResponse> records;
    private Long total;
    private Long pages;
    private Long current;

    public NotificationPageResponse(List<NotificationResponse> records, Long total, Long pages, Long current) {
        this.records = records;
        this.total = total;
        this.pages = pages;
        this.current = current;
    }
}
