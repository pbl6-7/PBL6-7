package com.campus.activity.service;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.entity.Comment;
import com.campus.activity.mapper.CommentMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    /**
     * 发布评论
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param request 评论请求
     * @return 评论响应
     */
    @Transactional
    public CommentResponse publishComment(Long activityId, Long userId, CommentRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        Comment comment = new Comment();
        comment.setActivityId(activityId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());

        commentMapper.insert(comment);

        CommentResponse response = CommentResponse.fromEntity(comment);
        response.setUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
        return response;
    }

    /**
     * 获取活动的评论列表
     * @param activityId 活动ID
     * @param page 页码
     * @param size 每页数量
     * @return 评论列表
     */
    public List<CommentResponse> getCommentList(Long activityId, Integer page, Integer size) {
        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 10;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Integer offset = (int) ((long) (page - 1) * size);

        List<Comment> comments = commentMapper.selectByActivityId(activityId, offset, size);

        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> userIds = new HashSet<>();
        for (Comment comment : comments) {
            userIds.add(comment.getUserId());
        }

        Map<Long, User> userMap = batchGetUsers(userIds);

        List<CommentResponse> responses = new ArrayList<>();
        for (Comment comment : comments) {
            CommentResponse response = CommentResponse.fromEntity(comment);
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                response.setUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
            responses.add(response);
        }

        return responses;
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此评论");
        }

        commentMapper.deleteById(commentId);
    }

    /**
     * 批量获取用户信息
     */
    private Map<Long, User> batchGetUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }
}
