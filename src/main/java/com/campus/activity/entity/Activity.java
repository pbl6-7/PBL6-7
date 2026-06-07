package com.campus.activity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 活动标题
     */
    private String title;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 活动地点
     */
    private String location;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 发布者ID
     */
    private Long publisherId;
    
    /**
     * 活动状态：draft-草稿，published-已发布，cancelled-已取消，ended-已结束
     */
    private String status;
    
    /**
     * 审核状态：pending-待审核，approved-已通过，rejected-已拒绝
     */
    private String approvalStatus;
    
    /**
     * 活动类型ID
     */
    private Long typeId;
    
    /**
     * 最大参与人数，0表示不限
     */
    private Integer maxParticipants;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}