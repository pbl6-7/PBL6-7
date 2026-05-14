package com.campus.activity.controller;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.service.CommentService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Api(tags = "评论管理")
public class CommentController {

    private final CommentService commentService;
    private final JwtUtils jwtUtils;

    @PostMapping("/activities/{activityId}/comments")
    @ApiOperation("发布评论")
    public Result<CommentResponse> publishComment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long activityId,
            @Valid @RequestBody CommentRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        CommentResponse response = commentService.publishComment(activityId, userId, request);
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
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long commentId) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        commentService.deleteComment(commentId, userId);
        return Result.success(null, "评论删除成功");
    }
}
