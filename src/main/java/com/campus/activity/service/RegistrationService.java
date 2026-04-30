package com.campus.activity.service;

import com.campus.activity.dto.RegistrationPageResponse;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityRegistration;
import com.campus.activity.mapper.ActivityRegistrationMapper;
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
public class RegistrationService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String APPROVAL_STATUS_APPROVED = "approved";
    private static final String STATUS_PUBLISHED = "published";
    private static final int MAX_PAGE_SIZE = 100;

    private final ActivityRegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    @Transactional
    public RegistrationResponse registerForActivity(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!APPROVAL_STATUS_APPROVED.equals(activity.getApprovalStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未通过审核，无法报名");
        }

        if (!STATUS_PUBLISHED.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未发布，无法报名");
        }

        if (activity.getStartTime() != null && activity.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动已开始或已结束，无法报名");
        }

        ActivityRegistration existing = registrationMapper.selectByActivityIdAndUserId(activityId, userId);
        if (existing != null) {
            throw new BusinessException(ResultCode.CONFLICT, "您已报名此活动");
        }

        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
            Long currentCount = registrationMapper.countByActivityId(activityId);
            if (currentCount >= activity.getMaxParticipants()) {
                throw new BusinessException(ResultCode.FORBIDDEN, "活动报名人数已达上限");
            }
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus(STATUS_PENDING);

        registrationMapper.insert(registration);

        RegistrationResponse response = RegistrationResponse.fromEntity(registration);
        fillActivityInfo(response, activity);
        User user = userMapper.selectById(userId);
        if (user != null) {
            response.setUserName(user.getRealName());
        }

        return response;
    }

    public RegistrationPageResponse getMyRegistrations(Long userId, Integer page, Integer size) {
        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 10;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        int offset = (page - 1) * size;

        Long total = registrationMapper.countByUserId(userId);

        List<ActivityRegistration> pagedList = registrationMapper.selectByUserIdWithPage(userId, offset, size);

        Set<Long> activityIds = pagedList.stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toSet());
        Set<Long> userIds = pagedList.stream()
                .map(ActivityRegistration::getUserId)
                .collect(Collectors.toSet());

        Map<Long, Activity> activityMap = batchGetActivities(activityIds);
        Map<Long, User> userMap = batchGetUsers(userIds);

        List<RegistrationResponse> responses = pagedList.stream()
                .map(reg -> {
                    RegistrationResponse response = RegistrationResponse.fromEntity(reg);
                    if (activityMap.containsKey(reg.getActivityId())) {
                        fillActivityInfo(response, activityMap.get(reg.getActivityId()));
                    }
                    fillUserInfo(response, userMap);
                    return response;
                })
                .collect(Collectors.toList());

        RegistrationPageResponse pageResponse = new RegistrationPageResponse();
        pageResponse.setList(responses);
        pageResponse.setTotal(total);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalPages((int) Math.ceil((double) total / size));

        return pageResponse;
    }

    public RegistrationPageResponse getActivityRegistrations(Long publisherId, Long activityId, Integer page, Integer size) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此活动的报名列表");
        }

        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 10;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        int offset = (page - 1) * size;

        List<ActivityRegistration> allRegistrations = registrationMapper.selectByActivityId(activityId);
        Long total = (long) allRegistrations.size();

        List<ActivityRegistration> pagedList = allRegistrations.stream()
                .skip(offset)
                .limit(size)
                .collect(Collectors.toList());

        Set<Long> userIds = pagedList.stream()
                .map(ActivityRegistration::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = batchGetUsers(userIds);

        List<RegistrationResponse> responses = pagedList.stream()
                .map(reg -> {
                    RegistrationResponse response = RegistrationResponse.fromEntity(reg);
                    fillActivityInfo(response, activity);
                    fillUserInfo(response, userMap);
                    return response;
                })
                .collect(Collectors.toList());

        RegistrationPageResponse pageResponse = new RegistrationPageResponse();
        pageResponse.setList(responses);
        pageResponse.setTotal(total);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalPages((int) Math.ceil((double) allRegistrations.size() / size));

        return pageResponse;
    }

    @Transactional
    public RegistrationResponse updateRegistrationStatus(Long publisherId, Long registrationId, String newStatus) {
        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在");
        }

        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此报名状态");
        }

        registration.setStatus(newStatus);
        registrationMapper.updateById(registration);

        RegistrationResponse response = RegistrationResponse.fromEntity(registration);
        fillActivityInfo(response, activity);
        User user = userMapper.selectById(registration.getUserId());
        if (user != null) {
            response.setUserName(user.getRealName());
        }

        return response;
    }

    @Transactional
    public void cancelRegistration(Long userId, Long activityId) {
        ActivityRegistration registration = registrationMapper.selectByActivityIdAndUserId(activityId, userId);
        if (registration == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在");
        }

        if (STATUS_CANCELLED.equals(registration.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该报名已取消");
        }

        registration.setStatus(STATUS_CANCELLED);
        registrationMapper.updateById(registration);
    }

    private Map<Long, Activity> batchGetActivities(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Map.of();
        }
        List<Activity> activities = activityMapper.selectByIds(new ArrayList<>(activityIds));
        return activities.stream().collect(Collectors.toMap(Activity::getId, a -> a));
    }

    private Map<Long, User> batchGetUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    private void fillActivityInfo(RegistrationResponse response, Activity activity) {
        if (activity != null) {
            response.setActivityTitle(activity.getTitle());
            response.setActivityStartTime(activity.getStartTime());
            response.setActivityEndTime(activity.getEndTime());
            response.setActivityLocation(activity.getLocation());
        }
    }

    private void fillUserInfo(RegistrationResponse response, Map<Long, User> userMap) {
        if (response.getUserId() != null && userMap.containsKey(response.getUserId())) {
            response.setUserName(userMap.get(response.getUserId()).getRealName());
        }
    }
}
