package com.campus.activity.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class Activity {

    private Long id;

    @NotBlank(message = "活动标题不能为空")
    @Length(max = 200, message = "活动标题不能超过200字符")
    private String title;

    @NotNull(message = "发布者ID不能为空")
    private Long publisherId;

    @NotNull(message = "活动开始时间不能为空")
    @Future(message = "活动开始时间必须是未来时间")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;

    @Length(max = 500, message = "活动地点不能超过500字符")
    private String location;

    @Length(max = 5000, message = "活动描述不能超过5000字符")
    private String description;

    private String status;

    private String approvalStatus;

    private Long typeId;

    @Range(min = 0, max = 100000, message = "最大参与人数必须在0-100000之间")
    private Integer maxParticipants;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
