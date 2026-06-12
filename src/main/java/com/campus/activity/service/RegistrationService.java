package com.campus.activity.service;

import com.campus.activity.dto.RegistrationPageResponse;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityRegistration;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BaseService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.ActivityStatusConstants;
import com.campus.core.constants.ApprovalStatusConstants;
import com.campus.core.constants.AuditOperationConstants;
import com.campus.core.constants.AuditResourceTypeConstants;
import com.campus.core.constants.NotificationTypeConstants;
import com.campus.core.constants.RegistrationStatusConstants;
import com.campus.core.service.AuditService;
import com.campus.core.util.BatchQueryUtils;
import com.campus.core.util.PageUtils;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动报名服务类
 * 继承 BaseService，使用公共方法和工具类
 */
@Service
@Slf4j
public class RegistrationService extends BaseService {

    @Autowired
    private ActivityRegistrationMapper registrationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    /**
     * 用户报名活动
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 报名响应对象
     * @throws BusinessException 当活动不存在、未审核、未发布、已开始或名额已满时抛出异常
     */
    @Transactional
    public RegistrationResponse registerForActivity(Long userId, Long activityId) {
        log.info("用户 {} 开始报名活动 {}", userId, activityId);

        Activity activity = activityMapper.selectById(activityId);
        validateNotNull(activity, ResultCode.NOT_FOUND, "活动不存在");

        validateActivityApproved(activity);
        validateActivityPublished(activity);
        validateActivityNotStarted(activity);

        ActivityRegistration existing = registrationMapper.selectByActivityIdAndUserId(activityId, userId);
        if (existing != null) {
            return handleExistingRegistration(existing, activity, userId);
        }

        checkActivityCapacity(activityId, activity.getMaxParticipants());

        ActivityRegistration registration = createNewRegistration(userId, activityId);
        log.info("用户 {} 报名活动 {} 成功: registrationId={}", userId, activityId, registration.getId());

        // 记录审计日志（报名成功）
        User user = userMapper.selectById(userId);
        String username = user != null ? user.getUsername() : null;
        auditService.quickRecord(userId, username, AuditOperationConstants.REGISTRATION_CREATE,
                AuditResourceTypeConstants.REGISTRATION, registration.getId(), 200, "报名活动成功: " + activity.getTitle());

        RegistrationResponse response = buildRegistrationResponse(registration, activity, userId);
        sendRegistrationNotifications(userId, activity, true);

        return response;
    }

    /**
     * 处理已存在的报名记录（重新报名）
     *
     * @param existing 已存在的报名记录
     * @param activity 活动对象
     * @param userId 用户ID
     * @return 报名响应对象
     */
    private RegistrationResponse handleExistingRegistration(
            ActivityRegistration existing, Activity activity, Long userId) {
        if (!RegistrationStatusConstants.CANCELLED.equals(existing.getStatus())) {
            log.warn("报名失败 - 用户已报名: userId={}, activityId={}", userId, activity.getId());
            throw new BusinessException(ResultCode.CONFLICT, "您已报名此活动");
        }

        checkActivityCapacity(activity.getId(), activity.getMaxParticipants());

        existing.setStatus(RegistrationStatusConstants.PENDING);
        existing.setRegistrationTime(LocalDateTime.now());
        registrationMapper.updateById(existing);

        RegistrationResponse response = buildRegistrationResponse(existing, activity, userId);
        sendRegistrationNotifications(userId, activity, false);

        log.info("用户 {} 重新报名活动 {} 成功", userId, activity.getId());
        return response;
    }

    /**
     * 检查活动报名容量
     *
     * @param activityId 活动ID
     * @param maxParticipants 最大参与人数
     * @throws BusinessException 当名额已满时抛出异常
     */
    private void checkActivityCapacity(Long activityId, Integer maxParticipants) {
        if (maxParticipants != null && maxParticipants > 0) {
            Long currentCount = activityMapper.countConfirmedRegistrations(activityId);

            if (currentCount >= maxParticipants) {
                log.warn("报名失败 - 活动报名人数已达上限: activityId={}, max={}, current={}",
                        activityId, maxParticipants, currentCount);
                throw new BusinessException(ResultCode.FORBIDDEN, "活动报名人数已达上限");
            }
        }
    }

    /**
     * 创建新的报名记录
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 新创建的报名记录
     */
    private ActivityRegistration createNewRegistration(Long userId, Long activityId) {
        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus(RegistrationStatusConstants.PENDING);
        registrationMapper.insert(registration);
        return registration;
    }

    /**
     * 构建报名响应对象
     *
     * @param registration 报名记录
     * @param activity 活动对象
     * @param userId 用户ID
     * @return 报名响应对象
     */
    private RegistrationResponse buildRegistrationResponse(
            ActivityRegistration registration, Activity activity, Long userId) {
        RegistrationResponse response = RegistrationResponse.fromEntity(registration);
        fillActivityInfo(response, activity);
        User user = userMapper.selectById(userId);
        if (user != null) {
            response.setUserName(user.getRealName());
        }
        return response;
    }

    /**
     * 发送报名通知
     *
     * @param userId 用户ID
     * @param activity 活动对象
     * @param isNew 是否为新报名
     */
    private void sendRegistrationNotifications(Long userId, Activity activity, boolean isNew) {
        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getRealName() : "用户" + userId;

        String userMessage = isNew
                ? "您已成功报名活动《" + activity.getTitle() + "》，请在活动开始前准时参加"
                : "您已重新报名活动《" + activity.getTitle() + "》，请在活动开始前准时参加";
        notificationService.notifyUser(userId, NotificationTypeConstants.APPROVAL_RESULT, userMessage);

        String publisherMessage = "用户【" + userName + "】"
                + (isNew ? "报名了您的活动" : "重新报名了您的活动")
                + "《" + activity.getTitle() + "》";
        notificationService.notifyUser(activity.getPublisherId(),
                NotificationTypeConstants.SUBSCRIPTION_STATUS, publisherMessage);
    }

    /**
     * 获取用户的报名列表（分页）
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页报名响应
     */
    public RegistrationPageResponse getMyRegistrations(Long userId, Integer page, Integer size) {
        PageUtils.PageParams params = PageUtils.validateAndNormalize(page, size);

        Long total = registrationMapper.countByUserId(userId);
        List<ActivityRegistration> pagedList = registrationMapper.selectByUserIdWithPage(
                userId, params.getOffset(), params.getSize());

        Set<Long> activityIds = pagedList.stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toSet());
        Set<Long> userIds = pagedList.stream()
                .map(ActivityRegistration::getUserId)
                .collect(Collectors.toSet());

        // 使用 BatchQueryUtils 批量获取活动信息
        Map<Long, Activity> activityMap = BatchQueryUtils.batchQueryToMap(
                ids -> activityMapper.selectByIds(ids),
                activityIds,
                Activity::getId
        );

        // 使用 BatchQueryUtils 批量获取用户信息
        Map<Long, User> userMap = BatchQueryUtils.batchQueryToMap(
                ids -> userMapper.selectBatchIds(ids),
                userIds,
                User::getId
        );

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

        return buildPageResponse(responses, total, params);
    }

    /**
     * 获取活动的报名列表（分页）
     *
     * @param publisherId 发布者ID
     * @param activityId 活动ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页报名响应
     * @throws BusinessException 当活动不存在或无权查看时抛出异常
     */
    public RegistrationPageResponse getActivityRegistrations(
            Long publisherId, Long activityId, Integer page, Integer size) {
        Activity activity = activityMapper.selectById(activityId);
        validateNotNull(activity, ResultCode.NOT_FOUND, "活动不存在");

        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此活动的报名列表");
        }

        PageUtils.PageParams params = PageUtils.validateAndNormalize(page, size);

        Long total = registrationMapper.countByActivityId(activityId);
        List<ActivityRegistration> pagedList = registrationMapper.selectByActivityIdWithPage(
                activityId, params.getOffset(), params.getSize());

        Set<Long> userIds = pagedList.stream()
                .map(ActivityRegistration::getUserId)
                .collect(Collectors.toSet());

        // 使用 BatchQueryUtils 批量获取用户信息
        Map<Long, User> userMap = BatchQueryUtils.batchQueryToMap(
                ids -> userMapper.selectBatchIds(ids),
                userIds,
                User::getId
        );

        List<RegistrationResponse> responses = pagedList.stream()
                .map(reg -> {
                    RegistrationResponse response = RegistrationResponse.fromEntity(reg);
                    fillActivityInfo(response, activity);
                    fillUserInfo(response, userMap);
                    return response;
                })
                .collect(Collectors.toList());

        return buildPageResponse(responses, total, params);
    }

    /**
     * 报名状态更新无验证
     */
    private static final Set<String> VALID_STATUSES = Set.of(
            RegistrationStatusConstants.PENDING,
            RegistrationStatusConstants.CONFIRMED,
            RegistrationStatusConstants.CANCELLED
    );

    /**
     * 更新报名状态
     *
     * @param publisherId 发布者ID
     * @param registrationId 报名记录ID
     * @param newStatus 新状态
     * @return 报名响应对象
     * @throws BusinessException 当状态无效、报名不存在或无权操作时抛出异常
     */
    @Transactional
    public RegistrationResponse updateRegistrationStatus(
            Long publisherId, Long registrationId, String newStatus) {
        log.info("发布者 {} 更新报名 {} 状态为 {}", publisherId, registrationId, newStatus);

        if (!VALID_STATUSES.contains(newStatus)) {
            log.warn("更新报名状态失败 - 无效状态: {}", newStatus);
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "无效的报名状态");
        }

        ActivityRegistration registration = registrationMapper.selectById(registrationId);
        validateNotNull(registration, ResultCode.NOT_FOUND, "报名记录不存在");

        Activity activity = activityMapper.selectById(registration.getActivityId());
        validateNotNull(activity, ResultCode.NOT_FOUND, "活动不存在");

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

        RegistrationResponse response = buildRegistrationResponse(registration, activity, registration.getUserId());
        sendStatusUpdateNotifications(registration.getUserId(), activity, newStatus, publisherId);

        return response;
    }

    /**
     * 发送状态更新通知
     *
     * @param userId 用户ID
     * @param activity 活动对象
     * @param newStatus 新状态
     * @param publisherId 发布者ID
     */
    private void sendStatusUpdateNotifications(
            Long userId, Activity activity, String newStatus, Long publisherId) {
        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getRealName() : "用户" + userId;

        String statusMessage = RegistrationStatusConstants.getDescription(newStatus);
        String userNotification = "您报名的活动《" + activity.getTitle() + "》状态已更新为：【" + statusMessage + "】";
        if (RegistrationStatusConstants.CONFIRMED.equals(newStatus)) {
            userNotification += "，请准时参加";
        } else if (RegistrationStatusConstants.CANCELLED.equals(newStatus)) {
            userNotification += "，如有疑问请联系活动发布者";
        }
        notificationService.notifyUser(userId, NotificationTypeConstants.APPROVAL_RESULT, userNotification);

        String publisherNotification = "活动《" + activity.getTitle() + "》的报名状态已更新：用户【" + userName
                + "】的状态已变更为【" + statusMessage + "】";
        notificationService.notifyUser(publisherId, NotificationTypeConstants.SUBSCRIPTION_STATUS, publisherNotification);
    }

    /**
     * 取消报名
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @throws BusinessException 当报名不存在或已取消时抛出异常
     */
    @Transactional
    public void cancelRegistration(Long userId, Long activityId) {
        log.info("用户 {} 开始取消活动 {} 的报名", userId, activityId);

        ActivityRegistration registration = registrationMapper.selectByActivityIdAndUserId(activityId, userId);
        validateNotNull(registration, ResultCode.NOT_FOUND, "报名记录不存在");

        if (RegistrationStatusConstants.CANCELLED.equals(registration.getStatus())) {
            log.warn("取消报名失败 - 报名已取消: registrationId={}", registration.getId());
            throw new BusinessException(ResultCode.BAD_REQUEST, "该报名已取消");
        }

        registration.setStatus(RegistrationStatusConstants.CANCELLED);
        registrationMapper.updateById(registration);
        log.info("用户 {} 取消活动 {} 的报名成功: registrationId={}",
                userId, activityId, registration.getId());

        // 记录审计日志（取消报名）
        User user = userMapper.selectById(userId);
        String username = user != null ? user.getUsername() : null;
        Activity activity = activityMapper.selectById(activityId);
        String activityTitle = activity != null ? activity.getTitle() : "未知活动";
        auditService.quickRecord(userId, username, AuditOperationConstants.REGISTRATION_CANCEL,
                AuditResourceTypeConstants.REGISTRATION, registration.getId(), 200, "取消报名: " + activityTitle);

        sendCancellationNotification(userId, activityId);
    }

    /**
     * 发送取消报名通知
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     */
    private void sendCancellationNotification(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        User user = userMapper.selectById(userId);
        if (activity != null && user != null) {
            String userName = user.getRealName() != null ? user.getRealName() : "用户" + userId;
            notificationService.notifyUser(activity.getPublisherId(),
                    NotificationTypeConstants.SUBSCRIPTION_STATUS,
                    "用户【" + userName + "】取消了活动《" + activity.getTitle() + "》的报名");
        }
    }

    /**
     * 获取指定活动的所有有效报名用户ID列表
     * 仅返回 pending 和 confirmed 状态的报名，已取消的不包含
     * 
     * @param activityId 活动ID
     * @return 有效报名用户ID列表
     */
    public List<Long> getRegisteredUserIds(Long activityId) {
        // 查询 pending 状态的报名
        List<ActivityRegistration> pendingRegs = registrationMapper.selectByActivityIdAndStatus(
                activityId, RegistrationStatusConstants.PENDING);
        // 查询 confirmed 状态的报名
        List<ActivityRegistration> confirmedRegs = registrationMapper.selectByActivityIdAndStatus(
                activityId, RegistrationStatusConstants.CONFIRMED);

        return java.util.stream.Stream.concat(
                pendingRegs.stream(), confirmedRegs.stream())
                .map(ActivityRegistration::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 验证活动是否已审核通过
     *
     * @param activity 活动对象
     * @throws BusinessException 当活动未审核通过时抛出异常
     */
    private void validateActivityApproved(Activity activity) {
        if (!ApprovalStatusConstants.APPROVED.equals(activity.getApprovalStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未通过审核");
        }
    }

    /**
     * 验证活动是否已发布
     *
     * @param activity 活动对象
     * @throws BusinessException 当活动未发布时抛出异常
     */
    private void validateActivityPublished(Activity activity) {
        if (!ActivityStatusConstants.PUBLISHED.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未发布");
        }
    }

    /**
     * 验证活动是否未开始
     *
     * @param activity 活动对象
     * @throws BusinessException 当活动已开始时抛出异常
     */
    private void validateActivityNotStarted(Activity activity) {
        if (activity.getStartTime() != null && activity.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动已开始或已结束");
        }
    }

    /**
     * 填充活动信息到响应对象
     *
     * @param response 报名响应对象
     * @param activity 活动对象
     */
    private void fillActivityInfo(RegistrationResponse response, Activity activity) {
        if (activity != null) {
            response.setActivityTitle(activity.getTitle());
            response.setActivityStartTime(activity.getStartTime());
            response.setActivityEndTime(activity.getEndTime());
            response.setActivityLocation(activity.getLocation());
        }
    }

    /**
     * 填充用户信息到响应对象
     *
     * @param response 报名响应对象
     * @param userMap 用户映射Map
     */
    private void fillUserInfo(RegistrationResponse response, Map<Long, User> userMap) {
        if (response.getUserId() != null && userMap.containsKey(response.getUserId())) {
            response.setUserName(userMap.get(response.getUserId()).getRealName());
        }
    }

    /**
     * 构建分页响应对象
     *
     * @param responses 响应列表
     * @param total 总记录数
     * @param params 分页参数
     * @return 分页响应对象
     */
    private RegistrationPageResponse buildPageResponse(
            List<RegistrationResponse> responses, Long total, PageUtils.PageParams params) {
        RegistrationPageResponse pageResponse = new RegistrationPageResponse();
        pageResponse.setList(responses);
        pageResponse.setTotal(total);
        pageResponse.setPage(params.getPage());
        pageResponse.setSize(params.getSize());
        pageResponse.setTotalPages(PageUtils.calculateTotalPages(total, params.getSize()));
        return pageResponse;
    }
}