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
    private final NotificationService notificationService;

    @Transactional
    public RegistrationResponse registerForActivity(Long userId, Long activityId) {
        log.info("用户 {} 开始报名活动 {}", userId, activityId);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("报名失败 - 活动不存在: activityId={}", activityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!APPROVAL_STATUS_APPROVED.equals(activity.getApprovalStatus())) {
            log.warn("报名失败 - 活动未通过审核: activityId={}", activityId);
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未通过审核，无法报名");
        }

        if (!STATUS_PUBLISHED.equals(activity.getStatus())) {
            log.warn("报名失败 - 活动未发布: activityId={}", activityId);
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未发布，无法报名");
        }

        if (activity.getStartTime() != null && activity.getStartTime().isBefore(LocalDateTime.now())) {
            log.warn("报名失败 - 活动已开始或已结束: activityId={}", activityId);
            throw new BusinessException(ResultCode.FORBIDDEN, "活动已开始或已结束，无法报名");
        }

        ActivityRegistration existing = registrationMapper.selectByActivityIdAndUserId(activityId, userId);
        if (existing != null) {
            if (!STATUS_CANCELLED.equals(existing.getStatus())) {
                log.warn("报名失败 - 用户已报名: userId={}, activityId={}", userId, activityId);
                throw new BusinessException(ResultCode.CONFLICT, "您已报名此活动");
            }
            existing.setStatus(STATUS_PENDING);
            existing.setRegistrationTime(LocalDateTime.now());
            registrationMapper.updateById(existing);

            RegistrationResponse response = RegistrationResponse.fromEntity(existing);
            fillActivityInfo(response, activity);
            User user = userMapper.selectById(userId);
            if (user != null) {
                response.setUserName(user.getRealName());
            }

            notificationService.notifyUser(userId,
                    NotificationService.TYPE_APPROVAL_RESULT,
                    "您已重新报名活动《" + activity.getTitle() + "》，请在活动开始前准时参加");

            String publisherMessage = "用户【" + (user != null ? user.getRealName() : "用户" + userId) + "】重新报名了您的活动《" + activity.getTitle() + "》";
            notificationService.notifyUser(activity.getPublisherId(),
                    NotificationService.TYPE_SUBSCRIPTION_STATUS,
                    publisherMessage);

            log.info("用户 {} 重新报名活动 {} 成功", userId, activityId);
            return response;
        }

        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
            Long currentCount = registrationMapper.countByActivityId(activityId);
            if (currentCount >= activity.getMaxParticipants()) {
                log.warn("报名失败 - 活动报名人数已达上限: activityId={}, max={}, current={}",
                        activityId, activity.getMaxParticipants(), currentCount);
                throw new BusinessException(ResultCode.FORBIDDEN, "活动报名人数已达上限");
            }
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus(STATUS_PENDING);

        registrationMapper.insert(registration);
        log.info("用户 {} 报名活动 {} 成功: registrationId={}", userId, activityId, registration.getId());

        RegistrationResponse response = RegistrationResponse.fromEntity(registration);
        fillActivityInfo(response, activity);
        User user = userMapper.selectById(userId);
        if (user != null) {
            response.setUserName(user.getRealName());
        }

        // Bug #2 修复：通知报名者
        notificationService.notifyUser(userId,
                NotificationService.TYPE_APPROVAL_RESULT,
                "您已成功报名活动《" + activity.getTitle() + "》，请在活动开始前准时参加");

        // Bug #2 修复：通知活动发布者有新用户报名
        String publisherMessage = "用户【" + (user != null ? user.getRealName() : "用户" + userId) + "】报名了您的活动《" + activity.getTitle() + "》";
        notificationService.notifyUser(activity.getPublisherId(),
                NotificationService.TYPE_SUBSCRIPTION_STATUS,
                publisherMessage);

        return response;
    }

    public RegistrationPageResponse getMyRegistrations(Long userId, Integer page, Integer size) {
        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 10;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        int offset = (int) ((long) (page - 1) * size);

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

        int offset = (int) ((long) (page - 1) * size);

        Long total = registrationMapper.countByActivityId(activityId);

        List<ActivityRegistration> pagedList = registrationMapper.selectByActivityIdWithPage(activityId, offset, size);

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
        pageResponse.setTotalPages((int) Math.ceil((double) total / size));

        return pageResponse;
    }

    /**
     * 报名状态更新无验证
     */
    private static final Set<String> VALID_STATUSES = Set.of("pending", "confirmed", "cancelled");

    @Transactional
    public RegistrationResponse updateRegistrationStatus(Long publisherId, Long registrationId, String newStatus) {
        log.info("发布者 {} 更新报名 {} 状态为 {}", publisherId, registrationId, newStatus);

        if (!VALID_STATUSES.contains(newStatus)) {
            log.warn("更新报名状态失败 - 无效状态: {}", newStatus);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "无效的报名状态");
        }

        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            log.error("更新报名状态失败 - 报名记录不存在: registrationId={}", registrationId);
            throw new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在");
        }

        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            log.error("更新报名状态失败 - 活动不存在: activityId={}", registration.getActivityId());
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!activity.getPublisherId().equals(publisherId)) {
            log.warn("更新报名状态失败 - 无权操作: publisherId={}, expectedPublisherId={}",
                    publisherId, activity.getPublisherId());
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此报名状态");
        }

        String oldStatus = registration.getStatus();
        registration.setStatus(newStatus);
        registrationMapper.updateById(registration);
        log.info("发布者 {} 更新报名 {} 状态从 {} 到 {} 成功",
                publisherId, registrationId, oldStatus, newStatus);

        RegistrationResponse response = RegistrationResponse.fromEntity(registration);
        fillActivityInfo(response, activity);
        User user = userMapper.selectById(registration.getUserId());
        if (user != null) {
            response.setUserName(user.getRealName());
        }

        // Bug #3 修复：通知报名者状态已更新
        String statusMessage = getStatusMessage(newStatus);
        String notificationMessage = "您报名的活动《" + activity.getTitle() + "》状态已更新为：【" + statusMessage + "】";
        if ("confirmed".equals(newStatus)) {
            notificationMessage += "，请准时参加";
        } else if ("cancelled".equals(newStatus)) {
            notificationMessage += "，如有疑问请联系活动发布者";
        }
        notificationService.notifyUser(registration.getUserId(),
                NotificationService.TYPE_APPROVAL_RESULT,
                notificationMessage);

        String publisherMessage = "活动《" + activity.getTitle() + "》的报名状态已更新：用户【" + (user != null ? user.getRealName() : "用户" + registration.getUserId()) + "】的状态已变更为【" + statusMessage + "】";
        notificationService.notifyUser(publisherId,
                NotificationService.TYPE_SUBSCRIPTION_STATUS,
                publisherMessage);

        return response;
    }

    /**
     * 获取状态对应的中文描述
     */
    private String getStatusMessage(String status) {
        switch (status) {
            case "pending":
                return "待确认";
            case "confirmed":
                return "已确认";
            case "cancelled":
                return "已取消";
            default:
                return status;
        }
    }

    @Transactional
    public void cancelRegistration(Long userId, Long activityId) {
        log.info("用户 {} 开始取消活动 {} 的报名", userId, activityId);

        ActivityRegistration registration = registrationMapper.selectByActivityIdAndUserId(activityId, userId);
        if (registration == null) {
            log.error("取消报名失败 - 报名记录不存在: userId={}, activityId={}", userId, activityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在");
        }

        if (STATUS_CANCELLED.equals(registration.getStatus())) {
            log.warn("取消报名失败 - 报名已取消: registrationId={}", registration.getId());
            throw new BusinessException(ResultCode.BAD_REQUEST, "该报名已取消");
        }

        registration.setStatus(STATUS_CANCELLED);
        registrationMapper.updateById(registration);
        log.info("用户 {} 取消活动 {} 的报名成功: registrationId={}", userId, activityId, registration.getId());

        // 通知活动发布者有用户取消报名
        Activity activity = activityMapper.selectById(activityId);
        User user = userMapper.selectById(userId);
        if (activity != null && user != null) {
            String userName = user.getRealName() != null ? user.getRealName() : "用户" + userId;
            notificationService.notifyUser(activity.getPublisherId(),
                    NotificationService.TYPE_SUBSCRIPTION_STATUS,
                    "用户【" + userName + "】取消了活动《" + activity.getTitle() + "》的报名");
        }
    }

    private Map<Long, Activity> batchGetActivities(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Map.of();
        }
        List<Activity> activities = activityMapper.selectByIds(new ArrayList<>(activityIds));
        return activities.stream().collect(Collectors.toMap(Activity::getId, a -> a, (v1, v2) -> v1));
    }

    private Map<Long, User> batchGetUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
        return users.stream().collect(Collectors.toMap(User::getId, u -> u, (v1, v2) -> v1));
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
