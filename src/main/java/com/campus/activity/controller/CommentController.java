package com.campus.activity.controller;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.service.CommentService;
import com.campus.core.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Api(tags = "评论管理")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/activities/{activityId}/comments")
    @ApiOperation("发布评论")
    public Result<CommentResponse> publishComment(
            HttpServletRequest request,
            @PathVariable Long activityId,
            @Valid @RequestBody CommentRequest commentRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        CommentResponse response = commentService.publishComment(activityId, userId, commentRequest);
        return Result.success(response, "评论发布成功");
    }

    @GetMapping("/activities/{activityId}/comments")
    @ApiOperation("获取评论列表")
    public Result<List<CommentResponse>> getCommentList(
            @PathVariable Long activityId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        List<CommentResponse> comments = commentService.getCommentList(activityId, page, size);
        return Result.success(comments);
    }

    @DeleteMapping("/comments/{commentId}")
    @ApiOperation("删除评论")
    public Result<Void> deleteComment(
            HttpServletRequest request,
            @PathVariable Long commentId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        commentService.deleteComment(commentId, userId);
        return Result.success(null, "评论删除成功");
    }
}
