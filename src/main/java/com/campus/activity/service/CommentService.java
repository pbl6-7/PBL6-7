package com.campus.activity.service;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Comment;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.CommentMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.core.common.SensitiveWordFilter;
import com.campus.core.config.AppConfig;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentMapper commentMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final AppConfig appConfig;

    /**
     * 发布评论
     */
    @Transactional
    public CommentResponse publishComment(Long activityId, Long userId, CommentRequest request) {
        log.info("用户 {} 开始发布评论: activityId={}", userId, activityId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("发布评论失败 - 用户不存在: userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            log.warn("发布评论失败 - 评论内容为空: userId={}, activityId={}", userId, activityId);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "评论内容不能为空");
        }
        if (request.getContent().length() > appConfig.getComment().getMaxContentLength()) {
            log.warn("发布评论失败 - 评论内容过长: userId={}, length={}, maxLength={}",
                    userId, request.getContent().length(), appConfig.getComment().getMaxContentLength());
            throw new BusinessException(ResultCode.VALIDATION_ERROR,
                    "评论内容不能超过" + appConfig.getComment().getMaxContentLength() + "字符");
        }

        if (sensitiveWordFilter.containsSensitiveWord(request.getContent())) {
            log.warn("发布评论失败 - 包含敏感词: userId={}", userId);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "评论内容包含敏感词，请修改后重试");
        }

        if (request.getReplyToId() != null) {
            Comment parentComment = commentMapper.selectById(request.getReplyToId());
            if (parentComment == null) {
                log.warn("发布评论失败 - 被回复的评论不存在: replyToId={}", request.getReplyToId());
                throw new BusinessException(ResultCode.NOT_FOUND, "被回复的评论不存在");
            }
            if (!parentComment.getActivityId().equals(activityId)) {
                log.warn("发布评论失败 - 被回复的评论不属于该活动: replyToId={}, activityId={}",
                        request.getReplyToId(), activityId);
                throw new BusinessException(ResultCode.VALIDATION_ERROR, "被回复的评论不属于该活动");
            }
        }

        Comment comment = new Comment();
        comment.setActivityId(activityId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setReplyToId(request.getReplyToId());

        commentMapper.insert(comment);
        log.info("评论发布成功: commentId={}, userId={}, activityId={}", comment.getId(), userId, activityId);

        CommentResponse response = CommentResponse.fromEntity(comment);
        response.setUsername(user.getRealName());
        return response;
    }

    /**
     * 获取活动的评论列表
     * 修复问题7：优化N+1查询问题，批量预加载评论和用户信息
     */
    public List<CommentResponse> getCommentList(Long activityId, Integer page, Integer size) {
        page = page != null && page > 0 ? page : appConfig.getPagination().getDefaultPageSize();
        size = size != null && size > 0 ? size : appConfig.getPagination().getDefaultPageSize();
        if (size > appConfig.getPagination().getMaxPageSize()) {
            size = appConfig.getPagination().getMaxPageSize();
        }

        Integer offset = (int) ((long) (page - 1) * size);

        List<Comment> rootComments = commentMapper.selectRootCommentsByActivityId(activityId, offset, size);

        if (rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> rootIds = rootComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // 批量获取所有回复，避免N+1查询
        List<Comment> allReplies = commentMapper.selectRepliesByParentIds(rootIds);

        // 构建评论ID到评论对象的映射，避免O(n*m)的线性查找
        Map<Long, Comment> commentMap = new HashMap<>();
        for (Comment root : rootComments) {
            commentMap.put(root.getId(), root);
        }
        for (Comment reply : allReplies) {
            commentMap.put(reply.getId(), reply);
        }

        Map<Long, Long> replyCountMap = new HashMap<>();
        for (Comment reply : allReplies) {
            Long parentId = reply.getReplyToId();
            replyCountMap.merge(parentId, 1L, Long::sum);
        }

        // 一次性收集所有需要的用户ID
        Set<Long> allUserIds = new HashSet<>();
        for (Comment root : rootComments) {
            allUserIds.add(root.getUserId());
        }
        for (Comment reply : allReplies) {
            allUserIds.add(reply.getUserId());
            // 通过Map查找被回复的评论，避免线性查找
            if (reply.getReplyToId() != null && commentMap.containsKey(reply.getReplyToId())) {
                allUserIds.add(commentMap.get(reply.getReplyToId()).getUserId());
            }
        }

        Map<Long, User> userMap = batchGetUsers(allUserIds);

        Map<Long, List<Comment>> repliesByParent = new HashMap<>();
        for (Comment reply : allReplies) {
            repliesByParent.computeIfAbsent(reply.getReplyToId(), k -> new ArrayList<>()).add(reply);
        }

        List<CommentResponse> responses = new ArrayList<>();
        for (Comment root : rootComments) {
            CommentResponse rootResponse = buildCommentResponse(root, userMap, replyCountMap);

            List<Comment> replies = repliesByParent.getOrDefault(root.getId(), new ArrayList<>());
            List<CommentResponse> replyResponses = new ArrayList<>();
            for (Comment reply : replies) {
                CommentResponse replyResponse = buildCommentResponse(reply, userMap, null);
                if (reply.getReplyToId() != null && commentMap.containsKey(reply.getReplyToId())) {
                    Comment replyToComment = commentMap.get(reply.getReplyToId());
                    User replyToUser = userMap.get(replyToComment.getUserId());
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
     * 修复问题1：添加活动发布者权限验证
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("用户 {} 开始删除评论: commentId={}", userId, commentId);

        User user = userMapper.selectById(userId);
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            log.error("删除评论失败 - 评论不存在: commentId={}", commentId);
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }

        Activity activity = activityMapper.selectById(comment.getActivityId());
        if (activity == null) {
            log.error("删除评论失败 - 活动不存在: activityId={}", comment.getActivityId());
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        boolean isOwner = comment.getUserId().equals(userId);
        boolean isAdmin = user != null && "admin".equals(user.getRole());
        boolean isPublisher = activity.getPublisherId().equals(userId);

        if (!isOwner && !isAdmin && !isPublisher) {
            log.warn("用户 {} 无权删除评论 {} （不是所有者、管理员或活动发布者）", userId, commentId);
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此评论");
        }

        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(commentId);
        collectAllReplies(commentId, idsToDelete);

        commentMapper.deleteBatchByIds(idsToDelete);
        log.info("用户 {} 成功删除评论: commentId={}, 删除总数={}", userId, commentId, idsToDelete.size());
    }

    /**
     * 递归收集所有子回复ID
     */
    private void collectAllReplies(Long parentId, List<Long> idsToDelete) {
        collectAllRepliesWithDepth(parentId, idsToDelete, 0);
    }

    private void collectAllRepliesWithDepth(Long parentId, List<Long> idsToDelete, int depth) {
        if (depth > appConfig.getComment().getMaxReplyDepth()) {
            log.warn("评论嵌套层数超限: maxDepth={}, currentDepth={}",
                    appConfig.getComment().getMaxReplyDepth(), depth);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "评论嵌套层数超限");
        }

        List<Comment> replies = commentMapper.selectAllRepliesByReplyToId(parentId);
        for (Comment reply : replies) {
            idsToDelete.add(reply.getId());
            collectAllRepliesWithDepth(reply.getId(), idsToDelete, depth + 1);
        }
    }

    /**
     * 批量获取用户信息
     * 修复问题14：使用merge函数处理重复键
     */
    private Map<Long, User> batchGetUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
        return users.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (v1, v2) -> v1));
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
