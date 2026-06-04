package com.campus.activity.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class Comment {

    private Long id;

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "评论内容不能为空")
    @Length(min = 1, max = 1000, message = "评论内容必须在1-1000字符之间")
    private String content;

    private Long replyToId;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}
