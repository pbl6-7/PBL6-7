package com.campus.activity.service;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.dto.TagResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityImage;
import com.campus.activity.entity.ActivityType;
import com.campus.activity.mapper.ActivityImageMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.activity.mapper.ActivitySubscriptionMapper;
import com.campus.activity.mapper.ActivityTypeMapper;
import com.campus.core.constants.ActivityStatusConstants;
import com.campus.core.constants.ApprovalStatusConstants;
import com.campus.core.constants.AuditOperationConstants;
import com.campus.core.constants.AuditResourceTypeConstants;
import com.campus.core.constants.UserRoleConstants;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.core.common.SensitiveWordFilter;
import com.campus.core.service.AuditService;
import com.campus.activity.service.NotificationService;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 活动服务类
 *
 * 提供活动相关的业务逻辑，包括缓存支持：
 * - 热门活动缓存
 * - 活动详情缓存
 * - 缓存自动失效
 * - 敏感词验证
 * - 审计日志记录
 */
@Slf4j
@Service
public class ActivityService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityImageMapper activityImageMapper;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ActivitySubscriptionMapper subscriptionMapper;

    @Autowired
    private ActivityTagService activityTagService;

    @Autowired
    private ActivityTypeMapper activityTypeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ActivityRegistrationMapper registrationMapper;

    /**
     * 获取热门活动列表（带缓存）
     *
     * @param limit 限制数量
     * @return 热门活动列表
     */
    public List<Activity> getHotActivities(int limit) {
        String cacheKey = String.format("hotActivity:list:%d", limit);

        return cacheService.getHotActivity(cacheKey, List.class, () -> {
            log.info("从数据库获取热门活动列表，limit={}", limit);
            return activityMapper.selectRecentActivities(limit);
        });
    }

    /**
     * 根据ID获取活动详情（带缓存）
     *
     * @param id 活动ID
     * @return 活动详情
     */
    public Optional<Activity> getActivityByIdOptional(Long id) {
        String cacheKey = String.format("hotActivity:detail:%d", id);

        Activity activity = cacheService.getHotActivity(cacheKey, Activity.class, () -> {
            log.info("从数据库获取活动详情，id={}", id);
            return activityMapper.selectById(id);
        });

        return Optional.ofNullable(activity);
    }

    /**
     * 根据ID获取活动响应
     *
     * @param id 活动ID
     * @return 活动响应DTO
     */
    public ActivityResponse getActivityById(Long id) {
        Activity activity = activityMapper.selectById(id);
        ActivityResponse response = ActivityResponse.fromEntity(activity);
        if (response != null) {
            response.setTags(activityTagService.getTagsByActivityId(id));
            enrichActivityResponse(response);
        }
        return response;
    }

    /**
     * 填充活动响应的关联字段（发布者名称、活动类型名称、当前报名人数）
     *
     * @param response 活动响应DTO
     */
    private void enrichActivityResponse(ActivityResponse response) {
        if (response == null) return;
        /* 填充发布者名称 */
        if (response.getPublisherId() != null && response.getPublisherName() == null) {
            User publisher = userMapper.selectById(response.getPublisherId());
            if (publisher != null) {
                response.setPublisherName(publisher.getRealName() != null ? publisher.getRealName() : publisher.getUsername());
            }
        }
        /* 填充活动类型名称 */
        if (response.getTypeId() != null && response.getActivityTypeName() == null) {
            ActivityType type = activityTypeMapper.selectById(response.getTypeId());
            if (type != null) {
                response.setActivityTypeName(type.getName());
            }
        }
        /* 填充当前报名人数（已确认的报名数） */
        if (response.getId() != null) {
            Long count = registrationMapper.countByActivityIdAndStatus(response.getId(), "confirmed");
            response.setCurrentParticipants(count != null ? count : 0L);
        }
    }

    /**
     * 创建活动（自动清除相关缓存）
     *
     * @param activity 活动信息
     * @return 创建后的活动
     */
    @Transactional
    public Activity createActivity(Activity activity) {
        log.info("创建活动：{}", activity.getTitle());

        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        activityMapper.insert(activity);

        cacheService.clearHotActivityCache();
        log.info("已清除热门活动列表缓存");

        return activity;
    }

    /**
     * 发布活动
     *
     * @param userId 用户ID
     * @param userRole 用户角色
     * @param publishRequest 发布请求
     * @return 活动响应DTO
     */
    @Transactional
    public ActivityResponse publishActivity(Long userId, String userRole, ActivityPublishRequest publishRequest) {
        log.info("用户 {} 发布活动：{}", userId, publishRequest.getTitle());

        // 敏感词验证
        validateSensitiveWords(publishRequest.getTitle(), "活动标题");
        validateSensitiveWords(publishRequest.getDescription(), "活动描述");
        validateSensitiveWords(publishRequest.getLocation(), "活动地点");

        Activity activity = new Activity();
        activity.setTitle(publishRequest.getTitle());
        activity.setDescription(publishRequest.getDescription());
        activity.setLocation(publishRequest.getLocation());
        activity.setStartTime(publishRequest.getStartTime());
        activity.setEndTime(publishRequest.getEndTime());
        activity.setTypeId(publishRequest.getTypeId());
        activity.setMaxParticipants(publishRequest.getMaxParticipants());
        activity.setPublisherId(userId);
        activity.setStatus(ActivityStatusConstants.DRAFT);
        activity.setApprovalStatus(ApprovalStatusConstants.PENDING);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        activityMapper.insert(activity);

        // 处理活动图片关联
        if (publishRequest.getImageIds() != null && !publishRequest.getImageIds().isEmpty()) {
            for (Long fileId : publishRequest.getImageIds()) {
                ActivityImage activityImage = ActivityImage.builder()
                        .activityId(activity.getId())
                        .fileId(fileId)
                        .displayOrder(publishRequest.getImageIds().indexOf(fileId))
                        .createdAt(LocalDateTime.now())
                        .build();
                activityImageMapper.insert(activityImage);
            }
        }

        // 处理活动标签关联
        if (publishRequest.getTags() != null && !publishRequest.getTags().isEmpty()) {
            activityTagService.createTagsForActivity(activity.getId(), publishRequest.getTags());
        }

        // 记录审计日志
        auditService.quickRecord(userId, null, AuditOperationConstants.ACTIVITY_PUBLISH,
                AuditResourceTypeConstants.ACTIVITY, activity.getId(), 200, "发布活动: " + activity.getTitle());

        // 清除缓存
        cacheService.clearHotActivityCache();

        // 构建响应并加载标签
        ActivityResponse response = ActivityResponse.fromEntity(activity);
        List<TagResponse> tags = activityTagService.getTagsByActivityId(activity.getId());
        response.setTags(tags);

        return response;
    }

    /**
     * 验证敏感词
     * 
     * @param content 待验证内容
     * @param fieldName 字段名称（用于错误消息）
     */
    private void validateSensitiveWords(String content, String fieldName) {
        if (content != null && !content.isEmpty() && sensitiveWordFilter.containsSensitiveWord(content)) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, fieldName + "包含敏感词，请修改后重试");
        }
    }

    /**
     * 更新活动（通过请求DTO）
     *
     * @param id 活动ID
     * @param userId 用户ID
     * @param updateRequest 更新请求
     * @return 活动响应DTO
     */
    @Transactional
    public ActivityResponse updateActivity(Long id, Long userId, ActivityPublishRequest updateRequest) {
        log.info("用户 {} 更新活动：id={}", userId, id);

        // 敏感词验证
        validateSensitiveWords(updateRequest.getTitle(), "活动标题");
        validateSensitiveWords(updateRequest.getDescription(), "活动描述");
        validateSensitiveWords(updateRequest.getLocation(), "活动地点");

        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        // 权限检查：只有活动发布者可以修改活动
        if (!java.util.Objects.equals(activity.getPublisherId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此活动");
        }

        activity.setTitle(updateRequest.getTitle());
        activity.setDescription(updateRequest.getDescription());
        activity.setLocation(updateRequest.getLocation());
        activity.setStartTime(updateRequest.getStartTime());
        activity.setEndTime(updateRequest.getEndTime());
        activity.setTypeId(updateRequest.getTypeId());
        activity.setMaxParticipants(updateRequest.getMaxParticipants());
        activity.setUpdatedAt(LocalDateTime.now());

        activityMapper.updateById(activity);

        // 更新活动图片关联
        if (updateRequest.getImageIds() != null) {
            // 删除旧的图片关联
            List<ActivityImage> oldImages = activityImageMapper.selectByActivityId(id);
            for (ActivityImage oldImage : oldImages) {
                activityImageMapper.deleteById(oldImage.getId());
            }
            // 添加新的图片关联
            for (Long fileId : updateRequest.getImageIds()) {
                ActivityImage activityImage = ActivityImage.builder()
                        .activityId(id)
                        .fileId(fileId)
                        .displayOrder(updateRequest.getImageIds().indexOf(fileId))
                        .createdAt(LocalDateTime.now())
                        .build();
                activityImageMapper.insert(activityImage);
            }
        }

        // 记录审计日志
        auditService.quickRecord(userId, null, AuditOperationConstants.ACTIVITY_UPDATE,
                AuditResourceTypeConstants.ACTIVITY, id, 200, "更新活动: " + activity.getTitle());

        // 通知订阅用户活动信息已更新
        notifySubscribers(id, "activity_updated", "活动信息更新",
                "您订阅的活动「" + activity.getTitle() + "」信息已更新，请查看最新详情。");

        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        cacheService.clearHotActivityCache();

        return ActivityResponse.fromEntity(activity);
    }

    /**
     * 删除活动
     * 活动发布者和管理员均可删除活动
     *
     * @param id 活动ID
     * @param userId 操作用户ID
     * @param role 操作用户角色
     */
    @Transactional
    public void deleteActivity(Long id, Long userId, String role) {
        log.info("用户 {} 删除活动：id={}", userId, id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        
        // 权限检查：活动发布者或管理员可以删除活动
        boolean isOwner = java.util.Objects.equals(activity.getPublisherId(), userId);
        boolean isAdmin = UserRoleConstants.ADMIN.equals(role);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此活动");
        }
        
        String activityTitle = activity.getTitle();
        activityMapper.deleteById(id);

        // 记录审计日志
        auditService.quickRecord(userId, null, AuditOperationConstants.ACTIVITY_DELETE,
                AuditResourceTypeConstants.ACTIVITY, id, 200, "删除活动: " + activityTitle);

        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        cacheService.clearHotActivityCache();
    }

    /**
     * 更新活动状态（自动清除缓存）
     *
     * @param id 活动ID
     * @param status 新状态
     */
    @Transactional
    public void updateActivityStatus(Long id, Integer status) {
        log.info("更新活动状态：id={}, status={}", id, status);

        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new com.campus.core.common.BusinessException(com.campus.core.common.ResultCode.NOT_FOUND, "活动不存在");
        }
        activity.setStatus(String.valueOf(status));
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);

        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        cacheService.clearHotActivityCache();
    }

    /**
     * 更新活动状态（带权限校验，返回ActivityResponse）
     * 安全修复：发布活动时必须先通过审核
     *
     * @param id 活动ID
     * @param userId 操作用户ID
     * @param status 新状态字符串
     * @return 活动响应
     */
    @Transactional
    public ActivityResponse updateActivityStatus(Long id, Long userId, String status) {
        log.info("更新活动状态：id={}, userId={}, status={}", id, userId, status);

        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new com.campus.core.common.BusinessException(com.campus.core.common.ResultCode.NOT_FOUND, "活动不存在");
        }
        if (!activity.getPublisherId().equals(userId)) {
            throw new com.campus.core.common.BusinessException(com.campus.core.common.ResultCode.FORBIDDEN, "无权操作此活动");
        }

        // 安全校验：发布活动时必须已通过审核
        if ("published".equals(status) && !ApprovalStatusConstants.APPROVED.equals(activity.getApprovalStatus())) {
            throw new com.campus.core.common.BusinessException(com.campus.core.common.ResultCode.FORBIDDEN, "活动未通过审核，无法发布");
        }

        // 状态流转校验：已取消或已结束的活动不能再变更状态
        String currentStatus = activity.getStatus();
        if ("cancelled".equals(currentStatus) || "ended".equals(currentStatus)) {
            throw new com.campus.core.common.BusinessException(com.campus.core.common.ResultCode.BAD_REQUEST, "已" + currentStatus + "的活动无法变更状态");
        }

        String oldStatus = activity.getStatus();
        activity.setStatus(status);
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);

        // 活动取消时通知订阅用户
        if ("cancelled".equals(status) && !"cancelled".equals(oldStatus)) {
            notifySubscribers(id, "activity_cancelled", "活动已取消",
                    "您订阅的活动「" + activity.getTitle() + "」已被发布者取消。");
        }

        // 活动结束时通知订阅用户
        if ("ended".equals(status) && !"ended".equals(oldStatus)) {
            notifySubscribers(id, "activity_ended", "活动已结束",
                    "您订阅的活动「" + activity.getTitle() + "」已结束，感谢您的关注。");
        }

        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        cacheService.clearHotActivityCache();

        return ActivityResponse.fromEntity(activity);
    }

    /**
     * 获取指定发布者的活动列表
     *
     * @param publisherId 发布者ID
     * @return 活动响应列表
     */
    public List<ActivityResponse> getActivitiesByPublisher(Long publisherId) {
        List<Activity> activities = activityMapper.selectByPublisherId(publisherId);
        return activities.stream().map(ActivityResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * 获取活动列表（带筛选和分页）
     *
     * @param userId 用户ID
     * @param queryRequest 查询请求
     * @return 活动分页响应
     */
    public ActivityPageResponse getActivityList(Long userId, ActivityQueryRequest queryRequest) {
        if (queryRequest.getPage() == null || queryRequest.getPage() < 1) queryRequest.setPage(1);
        if (queryRequest.getSize() == null || queryRequest.getSize() < 1) queryRequest.setSize(10);
        if (queryRequest.getSize() > 100) queryRequest.setSize(100);

        // 安全修复：普通用户查看活动列表时，默认只显示已审核通过的活动
        // 管理员或按发布者查询时可以看到所有状态的活动
        if (queryRequest.getApprovalStatus() == null || queryRequest.getApprovalStatus().trim().isEmpty()) {
            queryRequest.setApprovalStatus(ApprovalStatusConstants.APPROVED);
        }

        int page = queryRequest.getPage();
        int size = queryRequest.getSize() != null ? queryRequest.getSize() : 10;
        int offset = (page - 1) * size;

        List<Activity> activities = activityMapper.selectList(
                null,
                queryRequest.getKeyword(),
                queryRequest.getStatus(),
                queryRequest.getApprovalStatus(),
                queryRequest.getTypeId(),
                queryRequest.getLocation(),
                queryRequest.getStartTimeFrom(),
                queryRequest.getStartTimeTo(),
                queryRequest.getSortBy(),
                queryRequest.getSortOrder(),
                offset,
                size
        );

        Long total = activityMapper.count(
                null,
                queryRequest.getKeyword(),
                queryRequest.getStatus(),
                queryRequest.getApprovalStatus(),
                queryRequest.getTypeId(),
                queryRequest.getLocation(),
                queryRequest.getStartTimeFrom(),
                queryRequest.getStartTimeTo()
        );

        ActivityPageResponse response = new ActivityPageResponse();
        List<ActivityResponse> activityResponses = activities.stream()
                .map(activity -> {
                    ActivityResponse ar = ActivityResponse.fromEntity(activity);
                    ar.setTags(activityTagService.getTagsByActivityId(activity.getId()));
                    enrichActivityResponse(ar);
                    return ar;
                })
                .collect(Collectors.toList());
        response.setList(activityResponses);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages((int) Math.ceil((double) total / size));
        return response;
    }

    /**
     * 获取待审核活动列表
     *
     * @return 活动响应列表
     */
    public List<ActivityResponse> getPendingActivities() {
        List<Activity> activities = activityMapper.selectPendingActivities();
        return activities.stream().map(ActivityResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * 按审核状态获取活动列表
     *
     * @param approvalStatus 审核状态
     * @return 活动响应列表
     */
    public List<ActivityResponse> getActivitiesByApprovalStatus(String approvalStatus) {
        List<Activity> activities = activityMapper.selectByApprovalStatus(approvalStatus);
        return activities.stream().map(ActivityResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * 按活动状态获取活动列表
     *
     * @param status 活动状态（draft/published/cancelled/ended）
     * @return 活动响应列表
     */
    public List<ActivityResponse> getActivitiesByStatus(String status) {
        List<Activity> activities = activityMapper.selectByStatus(status);
        return activities.stream().map(activity -> {
            ActivityResponse response = ActivityResponse.fromEntity(activity);
            enrichActivityResponse(response);
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 审核通过活动
     *
     * @param id 活动ID
     * @param adminId 管理员ID
     * @return 活动响应DTO
     */
    @Transactional
    public ActivityResponse approveActivity(Long id, Long adminId) {
        log.info("审核通过活动：id={}, adminId={}", id, adminId);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        activity.setApprovalStatus(ApprovalStatusConstants.APPROVED);
        activity.setStatus("published"); // 审核通过后设置状态为已发布
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);

        // 记录审计日志
        auditService.quickRecord(adminId, null, AuditOperationConstants.ACTIVITY_APPROVE,
                AuditResourceTypeConstants.ACTIVITY, id, 200, "审核通过活动: " + activity.getTitle());

        // 发送通知给活动发布者
        notificationService.notifyUser(activity.getPublisherId(), "activity_approved", 
                "活动审核通过", "您的活动「" + activity.getTitle() + "」已审核通过，可以开始报名了！");

        // 通知订阅用户活动已审核通过
        notifySubscribers(id, "activity_approved", "活动审核通过",
                "您订阅的活动「" + activity.getTitle() + "」已审核通过，可以开始报名了！");

        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        cacheService.clearHotActivityCache();

        return ActivityResponse.fromEntity(activity);
    }

    /**
     * 审核拒绝活动
     *
     * @param id 活动ID
     * @param reason 拒绝原因
     * @param adminId 管理员ID
     * @return 活动响应DTO
     */
    @Transactional
    public ActivityResponse rejectActivity(Long id, String reason, Long adminId) {
        log.info("审核拒绝活动：id={}, reason={}, adminId={}", id, reason, adminId);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        activity.setApprovalStatus(ApprovalStatusConstants.REJECTED);
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);

        // 记录审计日志
        auditService.quickRecord(adminId, null, AuditOperationConstants.ACTIVITY_REJECT,
                AuditResourceTypeConstants.ACTIVITY, id, 200, "审核拒绝活动: " + activity.getTitle() + ", 原因: " + reason);

        // 发送通知给活动发布者
        notificationService.notifyUser(activity.getPublisherId(), "activity_rejected",
                "活动审核拒绝", "您的活动「" + activity.getTitle() + "」审核被拒绝。原因：" + reason);

        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        cacheService.clearHotActivityCache();

        return ActivityResponse.fromEntity(activity);
    }

    /**
     * 获取热门活动（按报名人数排序）
     * @param limit 返回数量限制
     * @return 热门活动列表
     */
    public List<Map<String, Object>> getHotActivitiesByRegistration(Integer limit) {
        return activityMapper.selectHotActivitiesByRegistration(limit);
    }

    /**
     * 获取热门活动（按收藏人数排序）
     * @param limit 返回数量限制
     * @return 热门活动列表
     */
    public List<Map<String, Object>> getHotActivitiesByCollection(Integer limit) {
        return activityMapper.selectHotActivitiesByCollection(limit);
    }

    /**
     * 获取热门活动（按浏览量排序）
     * @param limit 返回数量限制
     * @return 热门活动列表
     */
    public List<Map<String, Object>> getHotActivitiesByView(Integer limit) {
        return activityMapper.selectHotActivitiesByView(limit);
    }

    /**
     * 通知活动的所有订阅用户
     *
     * @param activityId 活动ID
     * @param type 通知类型
     * @param title 通知标题
     * @param message 通知内容
     */
    private void notifySubscribers(Long activityId, String type, String title, String message) {
        try {
            List<Long> subscriberIds = subscriptionMapper.selectUserIdsByActivityId(activityId);
            for (Long userId : subscriberIds) {
                try {
                    notificationService.notifyUser(userId, type, title, message);
                } catch (Exception e) {
                    log.warn("通知订阅用户失败: userId={}, activityId={}, type={}, error={}",
                            userId, activityId, type, e.getMessage());
                }
            }
            if (!subscriberIds.isEmpty()) {
                log.debug("已通知{}个订阅用户: activityId={}, type={}", subscriberIds.size(), activityId, type);
            }
        } catch (Exception e) {
            log.error("获取订阅用户列表失败: activityId={}, error={}", activityId, e.getMessage());
        }
    }
}
