package com.campus.activity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动图片关联实体类
 * 用于关联活动与图片文件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityImage {

    /**
     * 图片关联ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
