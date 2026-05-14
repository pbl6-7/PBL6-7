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

        if (request.getReplyToId() != null) {
            Comment parentComment = commentMapper.selectById(request.getReplyToId());
            if (parentComment == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "被回复的评论不存在");
            }
            if (!parentComment.getActivityId().equals(activityId)) {
                throw new BusinessException(ResultCode.VALIDATION_ERROR, "被回复的评论不属于该活动");
            }
        }

        Comment comment = new Comment();
        comment.setActivityId(activityId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setReplyToId(request.getReplyToId());

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

        List<Comment> rootComments = commentMapper.selectRootCommentsByActivityId(activityId, offset, size);

        if (rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> userIds = rootComments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
        List<Comment> allReplies = new ArrayList<>();
        for (Comment root : rootComments) {
            List<Comment> replies = commentMapper.selectRepliesByReplyToId(root.getId());
            allReplies.addAll(replies);
            userIds.addAll(replies.stream().map(Comment::getUserId).collect(Collectors.toSet()));
        }

        Map<Long, User> userMap = batchGetUsers(userIds);
        Map<Long, Long> replyCountMap = buildReplyCountMap(rootComments);

        List<CommentResponse> responses = new ArrayList<>();
        for (Comment root : rootComments) {
            CommentResponse rootResponse = buildCommentResponse(root, userMap, replyCountMap);

            List<Comment> replies = commentMapper.selectRepliesByReplyToId(root.getId());
            List<CommentResponse> replyResponses = new ArrayList<>();
            for (Comment reply : replies) {
                CommentResponse replyResponse = buildCommentResponse(reply, userMap, null);
                if (reply.getReplyToId() != null) {
                    User replyToUser = userMap.get(commentMapper.selectById(reply.getReplyToId()).getUserId());
                    replyResponse.setReplyToUsername(replyToUser != null ? replyToUser.getRealName() : null);
                }
                replyResponses.add(replyResponse);
            }
            rootResponse.setReplyCount(replyResponses.size());

            rootResponse.setReplies(replyResponses);
            responses.add(rootResponse);
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

        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(commentId);
        collectAllReplies(commentId, idsToDelete);

        commentMapper.deleteBatchByIds(idsToDelete);
    }

    /**
     * 递归收集所有子回复ID
     */
    private void collectAllReplies(Long parentId, List<Long> idsToDelete) {
        List<Comment> replies = commentMapper.selectAllRepliesByReplyToId(parentId);
        for (Comment reply : replies) {
            idsToDelete.add(reply.getId());
            collectAllReplies(reply.getId(), idsToDelete);
        }
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

    /**
     * 构建回复数量Map
     */
    private Map<Long, Long> buildReplyCountMap(List<Comment> rootComments) {
        return rootComments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        root -> (long) commentMapper.selectRepliesByReplyToId(root.getId()).size()
                ));
    }

    /**
     * 构建评论响应对象
     */
    private CommentResponse buildCommentResponse(Comment comment, Map<Long, User> userMap, Map<Long, Long> replyCountMap) {
        CommentResponse response = CommentResponse.fromEntity(comment);
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            response.setUsername(user.getRealName());
        }
        if (replyCountMap != null && replyCountMap.containsKey(comment.getId())) {
            response.setReplyCount(replyCountMap.get(comment.getId()).intValue());
        }
        return response;
    }
}
