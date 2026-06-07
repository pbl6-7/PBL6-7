package com.campus.activity.dto;

import com.campus.core.validation.annotation.NoSensitiveWord;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 评论请求DTO
 * 用于发布或回复评论时接收请求参数
 */
@Data
public class CommentRequest {

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空", groups = {CreateGroup.class})
    @Size(min = 1, max = 1000, message = "评论内容长度必须在1-1000个字符之间", groups = {CreateGroup.class})
    @NoSensitiveWord(message = "评论内容包含敏感词，请修改后重新提交", groups = {CreateGroup.class})
    private String content;

    /**
     * 回复的评论ID（可选，用于回复评论）
     */
    @Min(value = 1, message = "回复的评论ID必须大于0", groups = {CreateGroup.class})
    private Long replyToId;
}
