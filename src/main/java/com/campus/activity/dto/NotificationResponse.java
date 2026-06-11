package com.campus.activity.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知响应DTO
 * 用于返回通知信息给前端
 */
@Data
public class NotificationResponse {
    private Long id;
    private Long activityId;
    /** 通知标题 */
    private String title;
    private String type;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;

    public NotificationResponse(Long id, Long activityId, String title, String type,
                                String content, Boolean isRead, LocalDateTime createTime) {
        this.id = id;
        this.activityId = activityId;
        this.title = title;
        this.type = type;
        this.content = content;
        this.isRead = isRead;
        this.createTime = createTime;
    }
}
