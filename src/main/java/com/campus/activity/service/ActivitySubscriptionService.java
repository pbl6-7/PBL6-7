package com.campus.activity.service;

import com.campus.activity.dto.SubscriptionDetailResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivitySubscription;
import com.campus.activity.mapper.ActivitySubscriptionMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.ActivityStatusConstants;
import com.campus.core.constants.ApprovalStatusConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivitySubscriptionService {

    private final ActivitySubscriptionMapper subscriptionMapper;
    private final ActivityMapper activityMapper;

    /**
     * 订阅活动
     * 修复问题5：增加活动状态验证
     */
    @Transactional
    public void subscribeActivity(Long userId, Long activityId) {
        log.info("用户 {} 开始订阅活动 {}", userId, activityId);
        
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!ApprovalStatusConstants.APPROVED.equals(activity.getApprovalStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未通过审核，无法订阅");
        }

        if (!ActivityStatusConstants.PUBLISHED.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未发布，无法订阅");
        }

        ActivitySubscription existing = subscriptionMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已经订阅过该活动");
        }

        ActivitySubscription subscription = new ActivitySubscription();
        subscription.setUserId(userId);
        subscription.setActivityId(activityId);
        subscriptionMapper.insert(subscription);
        
        log.info("用户 {} 成功订阅活动 {}", userId, activityId);
    }

    /**
     * 取消订阅
     */
    @Transactional
    public void unsubscribeActivity(Long userId, Long activityId) {
        log.info("用户 {} 开始取消订阅活动 {}", userId, activityId);
        
        ActivitySubscription existing = subscriptionMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "尚未订阅该活动");
        }

        subscriptionMapper.deleteByUserIdAndActivityId(userId, activityId);
        
        log.info("用户 {} 成功取消订阅活动 {}", userId, activityId);
    }

    /**
     * 获取用户订阅列表
     */
    public List<ActivitySubscription> getUserSubscriptions(Long userId) {
        return subscriptionMapper.selectByUserId(userId);
    }

    /**
     * 获取用户订阅详情列表
     */
    public List<SubscriptionDetailResponse> getUserSubscriptionDetails(Long userId) {
        List<ActivitySubscription> subscriptions = subscriptionMapper.selectByUserId(userId);
        List<SubscriptionDetailResponse> details = new ArrayList<>();

        for (ActivitySubscription subscription : subscriptions) {
            Activity activity = activityMapper.selectById(subscription.getActivityId());
            SubscriptionDetailResponse detail = new SubscriptionDetailResponse(
                    subscription.getActivityId(),
                    activity != null ? activity.getTitle() : "未知活动",
                    activity != null ? activity.getLocation() : "未知地点",
                    activity != null ? activity.getStartTime() : null,
                    subscription.getCreateTime()
            );
            details.add(detail);
        }

        return details;
    }

    /**
     * 检查是否已订阅
     */
    public boolean isSubscribed(Long userId, Long activityId) {
        return subscriptionMapper.selectByUserIdAndActivityId(userId, activityId) != null;
    }

    /**
     * 获取活动订阅数
     */
    public int getSubscriptionCount(Long activityId) {
        return subscriptionMapper.countByActivityId(activityId);
    }

    /**
     * 获取活动的所有订阅用户ID
     */
    public List<Long> getSubscribedUserIds(Long activityId) {
        return subscriptionMapper.selectUserIdsByActivityId(activityId);
    }
}
