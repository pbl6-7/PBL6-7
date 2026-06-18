package com.campus.activity.controller;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Comment;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.CommentMapper;
import com.campus.activity.service.CommentService;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import com.campus.core.validation.group.CreateGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Api(tags = "评论管理")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final ActivityMapper activityMapper;

    @PostMapping("/activities/{activityId}/comments")
    @ApiOperation("发布评论")
    public Result<CommentResponse> publishComment(
            HttpServletRequest request,
            @PathVariable Long activityId,
            @Validated({CreateGroup.class}) @RequestBody CommentRequest commentRequest) {
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

    /**
     * 编辑评论
     * 只有评论所有者可以编辑自己的评论
     *
     * @param request HTTP请求对象
     * @param commentId 评论ID
     * @param body 请求体，包含新内容
     * @return 更新后的评论
     */
    @PutMapping("/comments/{commentId}")
    @ApiOperation("编辑评论")
    public Result<CommentResponse> updateComment(
            HttpServletRequest request,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "请先登录");
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST, "评论内容不能为空");
        }
        CommentResponse response = commentService.updateComment(commentId, userId, content);
        return Result.success(response, "评论编辑成功");
    }

    /**
     * 删除评论
     * 修复问题1：增加完整的权限验证
     * 只有评论所有者、管理员或活动发布者可以删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    @ApiOperation("删除评论")
    public Result<Void> deleteComment(
            HttpServletRequest request,
            @PathVariable Long commentId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String role = (String) request.getAttribute("currentUserRole");

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(ResultCode.NOT_FOUND, "评论不存在");
        }

        Activity activity = activityMapper.selectById(comment.getActivityId());
        if (activity == null) {
            return Result.error(ResultCode.NOT_FOUND, "活动不存在");
        }

        boolean isOwner = comment.getUserId().equals(userId);
        boolean isAdmin = UserRoleConstants.ADMIN.equals(role);
        boolean isPublisher = activity.getPublisherId().equals(userId);

        if (!isOwner && !isAdmin && !isPublisher) {
            return Result.error(ResultCode.FORBIDDEN, "无权删除此评论");
        }

        commentService.deleteComment(commentId, userId);
        return Result.success(null, "评论删除成功");
    }
}
