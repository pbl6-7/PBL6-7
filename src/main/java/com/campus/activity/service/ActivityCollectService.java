package com.campus.activity.service;

import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityCollect;
import com.campus.activity.mapper.ActivityCollectMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityCollectService {

    private static final String APPROVAL_STATUS_APPROVED = "approved";
    private static final String STATUS_PUBLISHED = "published";

    private final ActivityCollectMapper activityCollectMapper;
    private final ActivityMapper activityMapper;

    /**
     * 收藏活动
     * 修复问题5：增加活动状态验证
     */
    @Transactional
    public void collectActivity(Long userId, Long activityId) {
        log.info("用户 {} 开始收藏活动 {}", userId, activityId);
        
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        if (!APPROVAL_STATUS_APPROVED.equals(activity.getApprovalStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未通过审核，无法收藏");
        }

        if (!STATUS_PUBLISHED.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "活动未发布，无法收藏");
        }

        ActivityCollect existing = activityCollectMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已经收藏过该活动");
        }

        ActivityCollect collect = new ActivityCollect();
        collect.setUserId(userId);
        collect.setActivityId(activityId);
        activityCollectMapper.insert(collect);
        
        log.info("用户 {} 成功收藏活动 {}", userId, activityId);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void uncollectActivity(Long userId, Long activityId) {
        log.info("用户 {} 开始取消收藏活动 {}", userId, activityId);
        
        ActivityCollect existing = activityCollectMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "尚未收藏该活动");
        }

        activityCollectMapper.deleteByUserIdAndActivityId(userId, activityId);
        
        log.info("用户 {} 成功取消收藏活动 {}", userId, activityId);
    }

    /**
     * 获取用户收藏列表
     */
    public List<ActivityCollect> getUserCollects(Long userId) {
        return activityCollectMapper.selectByUserId(userId);
    }

    /**
     * 检查是否已收藏
     */
    public boolean isCollected(Long userId, Long activityId) {
        return activityCollectMapper.selectByUserIdAndActivityId(userId, activityId) != null;
    }

    /**
     * 获取活动收藏数
     */
    public int getCollectCount(Long activityId) {
        return activityCollectMapper.countByActivityId(activityId);
    }
}
