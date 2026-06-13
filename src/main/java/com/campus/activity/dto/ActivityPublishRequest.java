package com.campus.activity.dto;

import com.campus.core.validation.annotation.NoSensitiveWord;
import com.campus.core.validation.annotation.ValidActivityTime;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动发布请求DTO
 * 用于创建或更新活动时接收请求参数
 */
@Data
@ValidActivityTime(message = "活动开始时间必须早于结束时间", groups = {CreateGroup.class, UpdateGroup.class})
public class ActivityPublishRequest {

    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 2, max = 200, message = "活动名称长度必须在2-200个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    @NoSensitiveWord(message = "活动名称包含敏感词，请修改后重新提交", groups = {CreateGroup.class, UpdateGroup.class})
    private String title;

    /**
     * 活动开始时间
     */
    @NotNull(message = "活动开始时间不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Future(message = "活动开始时间必须是未来时间", groups = {CreateGroup.class})
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @NotNull(message = "活动结束时间不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Future(message = "活动结束时间必须是未来时间", groups = {CreateGroup.class})
    private LocalDateTime endTime;

    /**
     * 活动地点
     */
    @NotBlank(message = "活动地点不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(max = 500, message = "活动地点不能超过500个字符", groups = {CreateGroup.class, UpdateGroup.class})
    @NoSensitiveWord(message = "活动地点包含敏感词，请修改后重新提交", groups = {CreateGroup.class, UpdateGroup.class})
    private String location;

    /**
     * 活动描述
     */
    @Size(max = 5000, message = "活动描述不能超过5000个字符", groups = {CreateGroup.class, UpdateGroup.class})
    @NoSensitiveWord(message = "活动描述包含敏感词，请修改后重新提交", groups = {CreateGroup.class, UpdateGroup.class})
    private String description;

    /**
     * 活动类型ID
     */
    @NotNull(message = "活动类型不能为空", groups = {CreateGroup.class})
    @Min(value = 1, message = "活动类型ID必须大于0", groups = {CreateGroup.class, UpdateGroup.class})
    private Long typeId;

    /**
     * 最大参与人数
     */
    @Min(value = 1, message = "最大参与人数必须大于0", groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 100000, message = "最大参与人数不能超过100000", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer maxParticipants;

    /**
     * 活动标签列表
     */
    @Size(max = 10, message = "活动标签数量不能超过10个", groups = {CreateGroup.class, UpdateGroup.class})
    private List<String> tags;

    /**
     * 活动图片文件ID列表
     */
    private List<Long> imageIds;
}
