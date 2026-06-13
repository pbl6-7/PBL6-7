package com.campus.activity.dto;

import com.campus.core.validation.annotation.NoSensitiveWord;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 活动类型创建请求DTO
 * 用于创建新的活动类型时接收请求参数
 */
@Data
public class ActivityTypeCreateRequest {

    /**
     * 类型名称
     */
    @NotBlank(message = "类型名称不能为空", groups = {CreateGroup.class})
    @Size(min = 2, max = 50, message = "类型名称长度必须在2-50个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    @NoSensitiveWord(message = "类型名称包含敏感词，请修改后重新提交", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
}
