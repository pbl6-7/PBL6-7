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
        response.setUsername(user.getRealName());
        return response;
    }

    /**
     * 获取活动的评论列表
     */
    public List<CommentResponse> getCommentList(Long activityId, Integer page, Integer size) {
        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 10;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Integer offset = (page - 1) * size;

        List<Comment> comments = commentMapper.selectByActivityId(activityId, offset, size);

        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = batchGetUsers(userIds);

        List<CommentResponse> responses = new ArrayList<>();
        for (Comment comment : comments) {
            CommentResponse commentResponse = CommentResponse.fromEntity(comment);
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                commentResponse.setUsername(user.getRealName());
            }
            responses.add(commentResponse);
        }

        return responses;
    }

    /**
     * 删除评论
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
