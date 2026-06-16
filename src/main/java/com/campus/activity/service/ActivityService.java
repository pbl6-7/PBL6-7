package com.campus.activity.service;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.dto.TagResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityImage;
import com.campus.activity.mapper.ActivityImageMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.constants.ActivityStatusConstants;
import com.campus.core.constants.ApprovalStatusConstants;
import com.campus.core.constants.AuditOperationConstants;
import com.campus.core.constants.AuditResourceTypeConstants;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.core.common.SensitiveWordFilter;
import com.campus.core.service.AuditService;
import com.campus.activity.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private ActivityTagService activityTagService;

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
        }
        return response;
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

        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        cacheService.clearHotActivityCache();

        return ActivityResponse.fromEntity(activity);
    }

    /**
     * 删除活动（带权限验证）
     *
     * @param id 活动ID
     * @param userId 用户ID
     */
    @Transactional
    public void deleteActivity(Long id, Long userId) {
        log.info("用户 {} 删除活动：id={}", userId, id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        
        // 权限检查：只有活动发布者可以删除活动
        if (!java.util.Objects.equals(activity.getPublisherId(), userId)) {
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
        activity.setStatus(status);
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);

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
}
