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
import com.campus.core.constants.AuditOperationConstants;
import com.campus.core.constants.AuditResourceTypeConstants;
import com.campus.core.constants.UserRoleConstants;
import com.campus.core.service.AuditService;
import com.campus.core.util.BatchQueryUtils;
import com.campus.core.util.PageUtils;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务类
 * 使用 BatchQueryUtils 和 PageUtils 优化批量查询和分页
 */
@Service
@Slf4j
public class CommentService {

    private final CommentMapper commentMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final AppConfig appConfig;
    private final AuditService auditService;

    /**
     * 构造函数注入依赖
     *
     * @param commentMapper 评论Mapper
     * @param activityMapper 活动Mapper
     * @param userMapper 用户Mapper
     * @param sensitiveWordFilter 敏感词过滤器
     * @param appConfig 应用配置
     * @param auditService 审计服务
     */
    public CommentService(
            CommentMapper commentMapper,
            ActivityMapper activityMapper,
            UserMapper userMapper,
            SensitiveWordFilter sensitiveWordFilter,
            AppConfig appConfig,
            AuditService auditService) {
        this.commentMapper = commentMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.appConfig = appConfig;
        this.auditService = auditService;
    }

    /**
     * 发布评论
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param request 评论请求对象
     * @return 评论响应对象
     * @throws BusinessException 当用户不存在、内容为空、内容过长或包含敏感词时抛出异常
     */
    @Transactional
    public CommentResponse publishComment(Long activityId, Long userId, CommentRequest request) {
        log.info("用户 {} 开始发布评论: activityId={}", userId, activityId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("发布评论失败 - 用户不存在: userId={}", userId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        validateCommentContent(request.getContent(), userId);

        if (request.getReplyToId() != null) {
            validateReplyComment(request.getReplyToId(), activityId);
        }

        Comment comment = new Comment();
        comment.setActivityId(activityId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setReplyToId(request.getReplyToId());

        commentMapper.insert(comment);
        log.info("评论发布成功: commentId={}, userId={}, activityId={}", comment.getId(), userId, activityId);

        // 记录审计日志（评论发布）
        auditService.quickRecord(userId, user.getUsername(), AuditOperationConstants.COMMENT_CREATE,
                AuditResourceTypeConstants.COMMENT, comment.getId(), 200, "发布评论成功");

        CommentResponse response = CommentResponse.fromEntity(comment);
        response.setUsername(user.getRealName());
        return response;
    }

    /**
     * 验证评论内容
     *
     * @param content 评论内容
     * @param userId 用户ID
     * @throws BusinessException 当内容无效时抛出异常
     */
    private void validateCommentContent(String content, Long userId) {
        if (content == null || content.trim().isEmpty()) {
            log.warn("发布评论失败 - 评论内容为空: userId={}", userId);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "评论内容不能为空");
        }
        if (content.length() > appConfig.getComment().getMaxContentLength()) {
            log.warn("发布评论失败 - 评论内容过长: userId={}, length={}, maxLength={}",
                    userId, content.length(), appConfig.getComment().getMaxContentLength());
            throw new BusinessException(ResultCode.VALIDATION_ERROR,
                    "评论内容不能超过" + appConfig.getComment().getMaxContentLength() + "字符");
        }

        if (sensitiveWordFilter.containsSensitiveWord(content)) {
            log.warn("发布评论失败 - 包含敏感词: userId={}", userId);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "评论内容包含敏感词，请修改后重试");
        }
    }

    /**
     * 验证被回复的评论
     *
     * @param replyToId 被回复的评论ID
     * @param activityId 活动ID
     * @throws BusinessException 当被回复的评论不存在或不属于该活动时抛出异常
     */
    private void validateReplyComment(Long replyToId, Long activityId) {
        Comment parentComment = commentMapper.selectById(replyToId);
        if (parentComment == null) {
            log.warn("发布评论失败 - 被回复的评论不存在: replyToId={}", replyToId);
            throw new BusinessException(ResultCode.NOT_FOUND, "被回复的评论不存在");
        }
        if (!parentComment.getActivityId().equals(activityId)) {
            log.warn("发布评论失败 - 被回复的评论不属于该活动: replyToId={}, activityId={}",
                    replyToId, activityId);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "被回复的评论不属于该活动");
        }
    }

    /**
     * 获取活动的评论列表
     * 修复问题7：优化N+1查询问题，批量预加载评论和用户信息
     *
     * @param activityId 活动ID
     * @param page 页码
     * @param size 每页数量
     * @return 评论响应列表
     */
    public List<CommentResponse> getCommentList(Long activityId, Integer page, Integer size) {
        PageUtils.PageParams params = PageUtils.validateAndNormalize(
                page, size,
                appConfig.getPagination().getDefaultPageSize(),
                appConfig.getPagination().getMaxPageSize());

        List<Comment> rootComments = commentMapper.selectRootCommentsByActivityId(
                activityId, params.getOffset(), params.getSize());

        if (rootComments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> rootIds = rootComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // 批量获取所有回复，避免N+1查询
        List<Comment> allReplies = commentMapper.selectRepliesByParentIds(rootIds);

        // 构建评论ID到评论对象的映射
        Map<Long, Comment> commentMap = buildCommentMap(rootComments, allReplies);

        // 构建回复数量映射
        Map<Long, Long> replyCountMap = buildReplyCountMap(allReplies);

        // 一次性收集所有需要的用户ID
        Set<Long> allUserIds = collectAllUserIds(rootComments, allReplies, commentMap);

        // 使用 BatchQueryUtils 批量获取用户信息
        Map<Long, User> userMap = BatchQueryUtils.batchQueryToMap(
                ids -> userMapper.selectBatchIds(ids),
                allUserIds,
                User::getId
        );

        // 构建回复按父评论分组的映射
        Map<Long, List<Comment>> repliesByParent = groupRepliesByParent(allReplies);

        // 构建响应列表
        return buildCommentResponses(rootComments, repliesByParent, userMap, commentMap, replyCountMap);
    }

    /**
     * 构建评论ID到评论对象的映射
     *
     * @param rootComments 根评论列表
     * @param allReplies 所有回复列表
     * @return 评论映射Map
     */
    private Map<Long, Comment> buildCommentMap(List<Comment> rootComments, List<Comment> allReplies) {
        Map<Long, Comment> commentMap = new HashMap<>();
        for (Comment root : rootComments) {
            commentMap.put(root.getId(), root);
        }
        for (Comment reply : allReplies) {
            commentMap.put(reply.getId(), reply);
        }
        return commentMap;
    }

    /**
     * 构建回复数量映射
     *
     * @param allReplies 所有回复列表
     * @return 回复数量映射
     */
    private Map<Long, Long> buildReplyCountMap(List<Comment> allReplies) {
        Map<Long, Long> replyCountMap = new HashMap<>();
        for (Comment reply : allReplies) {
            Long parentId = reply.getReplyToId();
            replyCountMap.merge(parentId, 1L, Long::sum);
        }
        return replyCountMap;
    }

    /**
     * 收集所有用户ID
     *
     * @param rootComments 根评论列表
     * @param allReplies 所有回复列表
     * @param commentMap 评论映射
     * @return 用户ID集合
     */
    private Set<Long> collectAllUserIds(
            List<Comment> rootComments, List<Comment> allReplies, Map<Long, Comment> commentMap) {
        Set<Long> allUserIds = new HashSet<>();
        for (Comment root : rootComments) {
            allUserIds.add(root.getUserId());
        }
        for (Comment reply : allReplies) {
            allUserIds.add(reply.getUserId());
            if (reply.getReplyToId() != null && commentMap.containsKey(reply.getReplyToId())) {
                allUserIds.add(commentMap.get(reply.getReplyToId()).getUserId());
            }
        }
        return allUserIds;
    }

    /**
     * 将回复按父评论分组
     *
     * @param allReplies 所有回复列表
     * @return 父评论ID到回复列表的映射
     */
    private Map<Long, List<Comment>> groupRepliesByParent(List<Comment> allReplies) {
        Map<Long, List<Comment>> repliesByParent = new HashMap<>();
        for (Comment reply : allReplies) {
            repliesByParent.computeIfAbsent(reply.getReplyToId(), k -> new ArrayList<>()).add(reply);
        }
        return repliesByParent;
    }

    /**
     * 构建评论响应列表
     *
     * @param rootComments 根评论列表
     * @param repliesByParent 回复分组映射
     * @param userMap 用户映射
     * @param commentMap 评论映射
     * @param replyCountMap 回复数量映射
     * @return 评论响应列表
     */
    private List<CommentResponse> buildCommentResponses(
            List<Comment> rootComments,
            Map<Long, List<Comment>> repliesByParent,
            Map<Long, User> userMap,
            Map<Long, Comment> commentMap,
            Map<Long, Long> replyCountMap) {
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
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @throws BusinessException 当评论不存在或无权删除时抛出异常
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

        validateDeletePermission(user, comment, activity, userId);

        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(commentId);
        collectAllReplies(commentId, idsToDelete);

        commentMapper.deleteBatchByIds(idsToDelete);
        log.info("用户 {} 成功删除评论: commentId={}, 删除总数={}", userId, commentId, idsToDelete.size());

        // 记录审计日志（评论删除）
        String username = user != null ? user.getUsername() : null;
        auditService.quickRecord(userId, username, AuditOperationConstants.COMMENT_DELETE,
                AuditResourceTypeConstants.COMMENT, commentId, 200, "删除评论成功，删除总数: " + idsToDelete.size());
    }

    /**
     * 验证删除权限
     *
     * @param user 用户对象
     * @param comment 评论对象
     * @param activity 活动对象
     * @param userId 用户ID
     * @throws BusinessException 当无权删除时抛出异常
     */
    private void validateDeletePermission(User user, Comment comment, Activity activity, Long userId) {
        boolean isOwner = comment.getUserId().equals(userId);
        boolean isAdmin = user != null && UserRoleConstants.ADMIN.equals(user.getRole());
        boolean isPublisher = activity.getPublisherId().equals(userId);

        if (!isOwner && !isAdmin && !isPublisher) {
            log.warn("用户 {} 无权删除评论 {} （不是所有者、管理员或活动发布者）", userId, comment.getId());
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此评论");
        }
    }

    /**
     * 递归收集所有子回复ID
     *
     * @param parentId 父评论ID
     * @param idsToDelete 待删除ID列表
     */
    private void collectAllReplies(Long parentId, List<Long> idsToDelete) {
        collectAllRepliesWithDepth(parentId, idsToDelete, 0);
    }

    /**
     * 递归收集所有子回复ID（带深度限制）
     *
     * @param parentId 父评论ID
     * @param idsToDelete 待删除ID列表
     * @param depth 当前深度
     * @throws BusinessException 当嵌套层数超限时抛出异常
     */
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
     * 构建评论响应对象
     *
     * @param comment 评论对象
     * @param userMap 用户映射
     * @param replyCountMap 回复数量映射
     * @return 评论响应对象
     */
    private CommentResponse buildCommentResponse(
            Comment comment, Map<Long, User> userMap, Map<Long, Long> replyCountMap) {
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