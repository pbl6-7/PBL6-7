package com.campus.activity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CommentRequest {
    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long replyToId;
}
