package com.campus.activity.service;

import com.campus.activity.dto.SubscriptionDetailResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivitySubscription;
import com.campus.activity.mapper.ActivitySubscriptionMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动订阅服务层
 * 提供活动订阅相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ActivitySubscriptionService {

    private final ActivitySubscriptionMapper subscriptionMapper;
    private final ActivityMapper activityMapper;

    /**
     * 订阅活动
     * @param userId 用户ID
     * @param activityId 活动ID
     */
    @Transactional
    public void subscribeActivity(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        ActivitySubscription existing = subscriptionMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已经订阅过该活动");
        }

        ActivitySubscription subscription = new ActivitySubscription();
        subscription.setUserId(userId);
        subscription.setActivityId(activityId);
        subscriptionMapper.insert(subscription);
    }

    /**
     * 取消订阅
     * @param userId 用户ID
     * @param activityId 活动ID
     */
    @Transactional
    public void unsubscribeActivity(Long userId, Long activityId) {
        ActivitySubscription existing = subscriptionMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "尚未订阅该活动");
        }

        subscriptionMapper.deleteByUserIdAndActivityId(userId, activityId);
    }

    /**
     * 获取用户订阅列表
     * @param userId 用户ID
     * @return 订阅列表
     */
    public List<ActivitySubscription> getUserSubscriptions(Long userId) {
        return subscriptionMapper.selectByUserId(userId);
    }

    /**
     * 获取用户订阅详情列表
     * @param userId 用户ID
     * @return 订阅详情列表
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
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 是否已订阅
     */
    public boolean isSubscribed(Long userId, Long activityId) {
        return subscriptionMapper.selectByUserIdAndActivityId(userId, activityId) != null;
    }

    /**
     * 获取活动订阅数
     * @param activityId 活动ID
     * @return 订阅数
     */
    public int getSubscriptionCount(Long activityId) {
        return subscriptionMapper.countByActivityId(activityId);
    }

    /**
     * 获取活动的所有订阅用户ID
     * @param activityId 活动ID
     * @return 订阅用户ID列表
     */
    public List<Long> getSubscribedUserIds(Long activityId) {
        return subscriptionMapper.selectUserIdsByActivityId(activityId);
    }
}
