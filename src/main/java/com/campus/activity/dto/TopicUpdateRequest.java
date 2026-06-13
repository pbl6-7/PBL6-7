package com.campus.activity.dto;

import com.campus.core.validation.annotation.NoSensitiveWord;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 话题更新请求DTO
 * 用于更新话题标题时接收请求参数
 */
@Data
public class TopicUpdateRequest {

    /**
     * 话题标题
     */
    @NotBlank(message = "话题标题不能为空", groups = {UpdateGroup.class})
    @Size(min = 2, max = 100, message = "话题标题长度必须在2-100个字符之间", groups = {UpdateGroup.class})
    @NoSensitiveWord(message = "话题标题包含敏感词，请修改后重新提交", groups = {UpdateGroup.class})
    private String title;
}
