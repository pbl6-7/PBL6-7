package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体类
 * 用于存储用户收到的通知信息
 */
@Data
public class Notification {
    private Long id;
    private Long userId;
    private Long activityId;
    /** 通知标题（用于前端展示） */
    private String title;
    private String type;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
