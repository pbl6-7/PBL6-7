package com.campus.activity.service;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private static final String APPROVAL_STATUS_APPROVED = "approved";
    private static final String APPROVAL_STATUS_PENDING = "pending";
    private static final String APPROVAL_STATUS_REJECTED = "rejected";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_ENDED = "ended";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "created_at", "start_time", "end_time", "title", "status", "updated_at"
    );

    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ActivityTagService activityTagService;

    /**
     * 校验活动状态是否允许编辑
     */
    private void validateActivityEditable(Activity activity) {
        if (APPROVAL_STATUS_APPROVED.equals(activity.getApprovalStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "已审核通过的活动不允许修改，请重新提交审核");
        }
    }

    /**
     * 校验活动状态是否允许删除
     */
    private void validateActivityDeletable(Activity activity) {
        if (STATUS_ENDED.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "已结束的活动不允许删除");
        }
        if (APPROVAL_STATUS_APPROVED.equals(activity.getApprovalStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "已审核通过的活动不允许删除");
        }
    }

    /**
     * 校验活动开始时间
     */
    private void validateStartTime(LocalDateTime startTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "活动开始时间不能早于当前时间");
        }
    }

    /**
     * 检查同一发布者的活动时间是否冲突
     */
    private void checkTimeConflict(Long publisherId, LocalDateTime startTime, LocalDateTime endTime, Long excludeActivityId) {
        List<Activity> publisherActivities = activityMapper.selectByPublisherId(publisherId);
        for (Activity existing : publisherActivities) {
            if (existing.getDeletedAt() != null) {
                continue;
            }
            if (excludeActivityId != null && existing.getId().equals(excludeActivityId)) {
                continue;
            }
            if (startTime.isBefore(existing.getEndTime()) && endTime.isAfter(existing.getStartTime())) {
                throw new BusinessException(ResultCode.CONFLICT,
                        "活动时间与已有活动《" + existing.getTitle() + "》冲突");
            }
        }
    }

    /**
     * 批量查询用户信息，解决N+1问题
     */
    private Map<Long, User> batchGetUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
        return users.stream().collect(Collectors.toMap(User::getId, u -> u, (v1, v2) -> v1));
    }

    /**
     * 设置发布者名称
     */
    private void setPublisherName(ActivityResponse response, Map<Long, User> userMap) {
        if (response.getPublisherId() != null && userMap.containsKey(response.getPublisherId())) {
            response.setPublisherName(userMap.get(response.getPublisherId()).getRealName());
        }
    }

    /**
     * 批量设置发布者名称
     */
    private List<ActivityResponse> batchSetPublisherName(List<ActivityResponse> responses, Map<Long, User> userMap) {
        responses.forEach(response -> setPublisherName(response, userMap));
        return responses;
    }

    /**
     * 验证排序参数，防止 SQL 注入
     */
    private void validateSortParams(String sortBy, String sortOrder) {
        if (sortBy != null && !ALLOWED_SORT_FIELDS.contains(sortBy.toLowerCase())) {
            log.warn("无效的排序字段: {}", sortBy);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "不支持的排序字段: " + sortBy);
        }
        if (sortOrder != null && !sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            log.warn("无效的排序方向: {}", sortOrder);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "排序方向只能是 ASC 或 DESC");
        }
    }

    @Transactional
    public ActivityResponse publishActivity(Long publisherId, String userRole, ActivityPublishRequest request) {
        log.info("用户 {} 开始发布活动: {}", publisherId, request.getTitle());

        User publisher = userMapper.selectById(publisherId);
        if (publisher == null) {
            log.error("发布者不存在: userId={}", publisherId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "发布者不存在");
        }

        if (!"publisher".equals(userRole)) {
            log.warn("用户 {} 尝试以角色 {} 发布活动（非发布者）", publisherId, userRole);
            throw new BusinessException(ResultCode.NOT_PUBLISHER, "只有发布者角色才能发布活动");
        }

        validateStartTime(request.getStartTime());

        if (request.getEndTime().isBefore(request.getStartTime())) {
            log.warn("活动结束时间早于开始时间: startTime={}, endTime={}", request.getStartTime(), request.getEndTime());
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "活动结束时间不能早于开始时间");
        }

        checkTimeConflict(publisherId, request.getStartTime(), request.getEndTime(), null);

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setPublisherId(publisherId);
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setLocation(request.getLocation());
        activity.setDescription(request.getDescription());
        activity.setTypeId(request.getTypeId());
        activity.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 0);
        activity.setStatus(STATUS_PUBLISHED);
        activity.setApprovalStatus(APPROVAL_STATUS_PENDING);

        activityMapper.insert(activity);
        log.info("活动创建成功: activityId={}", activity.getId());

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            activityTagService.setActivityTags(activity.getId(), request.getTagIds());
        }

        ActivityResponse response = ActivityResponse.fromEntity(activity);
        response.setPublisherName(publisher.getRealName());
        response.setTags(activityTagService.getTagsByActivityId(activity.getId()));

        log.info("用户 {} 成功发布活动: activityId={}, title={}", publisherId, activity.getId(), activity.getTitle());
        return response;
    }

    public ActivityResponse getActivityById(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!APPROVAL_STATUS_APPROVED.equals(activity.getApprovalStatus()) 
                && !STATUS_PUBLISHED.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未发布或未通过审核");
        }

        ActivityResponse response = ActivityResponse.fromEntity(activity);
        User publisher = userMapper.selectById(activity.getPublisherId());
        if (publisher != null) {
            response.setPublisherName(publisher.getRealName());
        }
        response.setTags(activityTagService.getTagsByActivityId(id));
        return response;
    }

    public List<ActivityResponse> getActivitiesByPublisher(Long publisherId) {
        List<Activity> activities = activityMapper.selectByPublisherId(publisherId);
        
        Set<Long> userIds = activities.stream()
                .map(Activity::getPublisherId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = batchGetUsers(userIds);
        
        List<ActivityResponse> responses = activities.stream()
                .map(ActivityResponse::fromEntity)
                .collect(Collectors.toList());
        
        batchSetPublisherName(responses, userMap);
        
        for (ActivityResponse response : responses) {
            response.setTags(activityTagService.getTagsByActivityId(response.getId()));
        }
        
        return responses;
    }

    @Transactional
    public ActivityResponse updateActivity(Long activityId, Long publisherId, ActivityPublishRequest request) {
        log.info("用户 {} 开始更新活动: activityId={}", publisherId, activityId);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("活动不存在: activityId={}", activityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!activity.getPublisherId().equals(publisherId)) {
            log.warn("用户 {} 无权修改活动 {} （不是发布者）", publisherId, activityId);
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此活动");
        }

        validateActivityEditable(activity);

        if (request.getStartTime() != null) {
            validateStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null && request.getStartTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                log.warn("更新活动时结束时间早于开始时间: activityId={}", activityId);
                throw new BusinessException(ResultCode.VALIDATION_ERROR, "活动结束时间不能早于开始时间");
            }
        }

        checkTimeConflict(publisherId,
                request.getStartTime() != null ? request.getStartTime() : activity.getStartTime(),
                request.getEndTime() != null ? request.getEndTime() : activity.getEndTime(),
                activityId);

        if (request.getTitle() != null) {
            activity.setTitle(request.getTitle());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            activity.setLocation(request.getLocation());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getMaxParticipants() != null) {
            activity.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getTypeId() != null) {
            activity.setTypeId(request.getTypeId());
        }

        activity.setApprovalStatus(APPROVAL_STATUS_PENDING);
        activityMapper.updateById(activity);
        log.info("活动更新成功: activityId={}", activityId);

        if (request.getTagIds() != null) {
            activityTagService.setActivityTags(activityId, request.getTagIds());
        }

        notificationService.notifySubscribersWithTitle(
                activityId,
                NotificationService.TYPE_ACTIVITY_UPDATE,
                "活动信息已更新，请查看最新信息"
        );

        ActivityResponse response = ActivityResponse.fromEntity(activity);
        User publisher = userMapper.selectById(activity.getPublisherId());
        if (publisher != null) {
            response.setPublisherName(publisher.getRealName());
        }
        response.setTags(activityTagService.getTagsByActivityId(activityId));

        log.info("用户 {} 成功更新活动: activityId={}", publisherId, activityId);
        return response;
    }

    @Transactional
    public void deleteActivity(Long activityId, Long publisherId) {
        log.info("用户 {} 开始删除活动: activityId={}", publisherId, activityId);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("活动不存在: activityId={}", activityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!activity.getPublisherId().equals(publisherId)) {
            log.warn("用户 {} 无权删除活动 {} （不是发布者）", publisherId, activityId);
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此活动");
        }

        validateActivityDeletable(activity);

        activity.setDeletedAt(LocalDateTime.now());
        activityMapper.updateById(activity);

        log.info("用户 {} 成功删除活动: activityId={}", publisherId, activityId);
    }

    @Transactional
    /**
     * 分页查询活动列表
     */
    public ActivityPageResponse getActivityList(Long publisherId, ActivityQueryRequest request) {
        Integer page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        Integer size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        String sortBy = request.getSortBy();
        String sortOrder = request.getSortOrder();

        validateSortParams(sortBy, sortOrder);

        Integer offset = (int) ((long) (page - 1) * size);

        List<Activity> activities = activityMapper.selectList(
                publisherId,
                request.getKeyword(),
                request.getStatus(),
                request.getApprovalStatus(),
                request.getTypeId(),
                request.getLocation(),
                request.getStartTimeFrom(),
                request.getStartTimeTo(),
                sortBy,
                sortOrder,
                offset,
                size
        );

        Long total = activityMapper.count(
                publisherId,
                request.getKeyword(),
                request.getStatus(),
                request.getApprovalStatus(),
                request.getTypeId(),
                request.getLocation(),
                request.getStartTimeFrom(),
                request.getStartTimeTo()
        );

        Set<Long> userIds = activities.stream()
                .map(Activity::getPublisherId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = batchGetUsers(userIds);

        List<ActivityResponse> activityResponses = activities.stream()
                .map(ActivityResponse::fromEntity)
                .collect(Collectors.toList());
        
        batchSetPublisherName(activityResponses, userMap);

        for (ActivityResponse response : activityResponses) {
            response.setTags(activityTagService.getTagsByActivityId(response.getId()));
        }

        ActivityPageResponse pageResponse = new ActivityPageResponse();
        pageResponse.setList(activityResponses);
        pageResponse.setTotal(total);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalPages((int) Math.ceil((double) total / size));

        return pageResponse;
    }

    /**
     * 获取待审核活动列表
     * @return 待审核活动列表
     */
    public List<ActivityResponse> getPendingActivities() {
        List<Activity> activities = activityMapper.selectPendingActivities();
        
        Set<Long> userIds = activities.stream()
                .map(Activity::getPublisherId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = batchGetUsers(userIds);
        
        List<ActivityResponse> responses = activities.stream()
                .map(ActivityResponse::fromEntity)
                .collect(Collectors.toList());
        
        return batchSetPublisherName(responses, userMap);
    }

    /**
     * 按审核状态获取活动列表
     * @param approvalStatus 审核状态
     * @return 活动列表
     */
    public List<ActivityResponse> getActivitiesByApprovalStatus(String approvalStatus) {
        List<Activity> activities = activityMapper.selectByApprovalStatus(approvalStatus);
        
        Set<Long> userIds = activities.stream()
                .map(Activity::getPublisherId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = batchGetUsers(userIds);
        
        List<ActivityResponse> responses = activities.stream()
                .map(ActivityResponse::fromEntity)
                .collect(Collectors.toList());
        
        return batchSetPublisherName(responses, userMap);
    }

    /**
     * 审核通过活动
     * @param activityId 活动ID
     * @return 活动响应
     */
    @Transactional
    public ActivityResponse approveActivity(Long activityId) {
        log.info("管理员审核活动: activityId={}, action=approve", activityId);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("审核活动失败 - 活动不存在: activityId={}", activityId);
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        if (!APPROVAL_STATUS_PENDING.equals(activity.getApprovalStatus())) {
            log.warn("审核活动失败 - 活动不在待审核状态: activityId={}, currentStatus={}",
                    activityId, activity.getApprovalStatus());
            throw new BusinessException(ResultCode.ACTIVITY_NOT_PENDING);
        }

        activity.setApprovalStatus(APPROVAL_STATUS_APPROVED);
        activity.setStatus(STATUS_PUBLISHED);
        activityMapper.updateById(activity);

        User publisher = userMapper.selectById(activity.getPublisherId());
        notificationService.notifyUser(
                activity.getPublisherId(),
                NotificationService.TYPE_APPROVAL_RESULT,
                "您的活动《" + activity.getTitle() + "》已通过审核"
        );

        ActivityResponse response = ActivityResponse.fromEntity(activity);
        if (publisher != null) {
            response.setPublisherName(publisher.getRealName());
        }

        log.info("活动审核通过: activityId={}, publisherId={}", activityId, activity.getPublisherId());
        return response;
    }

    /**
     * 审核拒绝活动
     * @param activityId 活动ID
     * @param reason 拒绝原因
     * @return 活动响应
     */
    @Transactional
    public ActivityResponse rejectActivity(Long activityId, String reason) {
        log.info("管理员审核活动: activityId={}, action=reject, reason={}", activityId, reason);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("审核活动失败 - 活动不存在: activityId={}", activityId);
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        if (!APPROVAL_STATUS_PENDING.equals(activity.getApprovalStatus())) {
            log.warn("审核活动失败 - 活动不在待审核状态: activityId={}, currentStatus={}",
                    activityId, activity.getApprovalStatus());
            throw new BusinessException(ResultCode.ACTIVITY_NOT_PENDING);
        }

        activity.setApprovalStatus(APPROVAL_STATUS_REJECTED);
        activityMapper.updateById(activity);

        User publisher = userMapper.selectById(activity.getPublisherId());
        String notificationMessage = "您的活动《" + activity.getTitle() + "》未通过审核";
        if (reason != null && !reason.trim().isEmpty()) {
            notificationMessage += "。原因：" + reason;
        }
        notificationService.notifyUser(
                activity.getPublisherId(),
                NotificationService.TYPE_APPROVAL_RESULT,
                notificationMessage
        );

        ActivityResponse response = ActivityResponse.fromEntity(activity);
        if (publisher != null) {
            response.setPublisherName(publisher.getRealName());
        }

        log.info("活动审核拒绝: activityId={}, publisherId={}, reason={}",
                activityId, activity.getPublisherId(), reason);
        return response;
    }
}
