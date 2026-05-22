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
public class ActivityService {

    private static final String APPROVAL_STATUS_APPROVED = "approved";
    private static final String APPROVAL_STATUS_PENDING = "pending";
    private static final String APPROVAL_STATUS_REJECTED = "rejected";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_ENDED = "ended";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final int MAX_PAGE_SIZE = 100;

    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

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
     * 批量查询用户信息，解决N+1问题
     */
    private Map<Long, User> batchGetUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
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

    @Transactional
    public ActivityResponse publishActivity(Long publisherId, ActivityPublishRequest request) {
        User publisher = userMapper.selectById(publisherId);
        if (publisher == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "发布者不存在");
        }

        validateStartTime(request.getStartTime());

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "活动结束时间不能早于开始时间");
        }

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setPublisherId(publisherId);
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setLocation(request.getLocation());
        activity.setDescription(request.getDescription());
        activity.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 0);
        activity.setStatus(STATUS_PUBLISHED);
        activity.setApprovalStatus(APPROVAL_STATUS_PENDING);

        activityMapper.insert(activity);

        ActivityResponse response = ActivityResponse.fromEntity(activity);
        response.setPublisherName(publisher.getRealName());
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
        
        return batchSetPublisherName(responses, userMap);
    }

    @Transactional
    public ActivityResponse updateActivity(Long activityId, Long publisherId, ActivityPublishRequest request) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此活动");
        }

        validateActivityEditable(activity);

        if (request.getStartTime() != null) {
            validateStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null && request.getStartTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new BusinessException(ResultCode.VALIDATION_ERROR, "活动结束时间不能早于开始时间");
            }
        }

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

        activity.setApprovalStatus(APPROVAL_STATUS_PENDING);
        activityMapper.updateById(activity);

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
        return response;
    }

    @Transactional
    public void deleteActivity(Long activityId, Long publisherId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此活动");
        }

        validateActivityDeletable(activity);

        activityMapper.deleteById(activityId);
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

        Integer offset = (int) ((long) (page - 1) * size);

        List<Activity> activities = activityMapper.selectList(
                publisherId,
                request.getKeyword(),
                request.getStatus(),
                request.getApprovalStatus(),
                request.getActivityType(),
                request.getLocation(),
                request.getStartTimeFrom(),
                request.getStartTimeTo(),
                request.getTagId(),
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
                request.getActivityType(),
                request.getLocation(),
                request.getStartTimeFrom(),
                request.getStartTimeTo(),
                request.getTagId()
        );

        Set<Long> userIds = activities.stream()
                .map(Activity::getPublisherId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = batchGetUsers(userIds);

        List<ActivityResponse> activityResponses = activities.stream()
                .map(ActivityResponse::fromEntity)
                .collect(Collectors.toList());
        
        batchSetPublisherName(activityResponses, userMap);

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
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        if (!APPROVAL_STATUS_PENDING.equals(activity.getApprovalStatus())) {
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
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        if (!APPROVAL_STATUS_PENDING.equals(activity.getApprovalStatus())) {
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
        return response;
    }
}
