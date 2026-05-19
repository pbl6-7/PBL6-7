package com.campus.activity.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知响应DTO
 * 用于返回通知信息
 */
@Data
public class NotificationResponse {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private String type;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;

    public NotificationResponse(Long id, Long activityId, String activityTitle, String type,
                                String content, Boolean isRead, LocalDateTime createTime) {
        this.id = id;
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.type = type;
        this.content = content;
        this.isRead = isRead;
        this.createTime = createTime;
    }
}
