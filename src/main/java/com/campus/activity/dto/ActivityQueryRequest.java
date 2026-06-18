package com.campus.activity.dto;

import com.campus.core.validation.annotation.NoSensitiveWord;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 活动查询请求DTO
 * 用于查询活动列表时接收请求参数
 */
@Data
public class ActivityQueryRequest {

    /**
     * 搜索关键词
     */
    @Size(max = 200, message = "搜索关键词不能超过200个字符")
    @NoSensitiveWord(message = "搜索关键词包含敏感词")
    private String keyword;

    /**
     * 活动状态
     */
    @Pattern(regexp = "^(?i)(PUBLISHED|DRAFT|CANCELLED|ONGOING|ENDED)$", message = "活动状态只能是PUBLISHED、DRAFT、CANCELLED、ONGOING或ENDED")
    private String status;

    /**
     * 审批状态
     */
    @Pattern(regexp = "^(?i)(PENDING|APPROVED|REJECTED)$", message = "审批状态只能是PENDING、APPROVED或REJECTED")
    private String approvalStatus;

    /**
     * 活动类型（用于搜索过滤）
     */
    @Size(max = 50, message = "活动类型不能超过50个字符")
    private String type;

    /**
     * 活动类型ID
     */
    @Min(value = 1, message = "活动类型ID必须大于0")
    private Long typeId;

    /**
     * 活动地点
     */
    @Size(max = 500, message = "活动地点不能超过500个字符")
    @NoSensitiveWord(message = "活动地点包含敏感词")
    private String location;

    /**
     * 开始时间范围（起始）
     */
    private LocalDateTime startTimeFrom;

    /**
     * 开始时间范围（结束）
     */
    private LocalDateTime startTimeTo;

    /**
     * 最小参与人数
     */
    @Min(value = 0, message = "最小参与人数不能小于0")
    private Integer minParticipants;

    /**
     * 最大参与人数
     */
    @Max(value = 100000, message = "最大参与人数不能超过100000")
    private Integer maxParticipants;

    /**
     * 排序字段
     */
    @Pattern(regexp = "^(createdAt|startTime|endTime|participantsCount|maxParticipants)$", message = "排序字段只能是createdAt、startTime、endTime、participantsCount或maxParticipants")
    private String sortBy;

    /**
     * 排序方向
     */
    @Pattern(regexp = "^(asc|desc)$", message = "排序方向只能是asc或desc")
    private String sortOrder;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size;
}
