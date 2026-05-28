package com.campus.activity.dto;

import com.campus.activity.entity.Comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private Long activityId;
    private Long userId;
    private String username;
    private String content;
    private Long replyToId;
    private String replyToUsername;
    private LocalDateTime createdAt;
    private Integer replyCount;
    private List<CommentResponse> replies;

    public static CommentResponse fromEntity(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setActivityId(comment.getActivityId());
        response.setUserId(comment.getUserId());
        response.setContent(comment.getContent());
        response.setReplyToId(comment.getReplyToId());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
