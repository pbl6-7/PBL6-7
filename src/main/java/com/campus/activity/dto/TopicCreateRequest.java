package com.campus.activity.dto;

import com.campus.core.validation.annotation.NoSensitiveWord;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 话题创建请求DTO
 * 用于创建活动话题时接收请求参数
 */
@Data
public class TopicCreateRequest {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空", groups = {CreateGroup.class})
    @Min(value = 1, message = "活动ID必须大于0", groups = {CreateGroup.class})
    private Long activityId;

    /**
     * 话题标题
     */
    @NotBlank(message = "话题标题不能为空", groups = {CreateGroup.class})
    @Size(min = 2, max = 100, message = "话题标题长度必须在2-100个字符之间", groups = {CreateGroup.class})
    @NoSensitiveWord(message = "话题标题包含敏感词，请修改后重新提交", groups = {CreateGroup.class})
    private String title;
}
