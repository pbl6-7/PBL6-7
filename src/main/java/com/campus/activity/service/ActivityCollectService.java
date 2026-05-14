package com.campus.activity.service;

import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityCollect;
import com.campus.activity.mapper.ActivityCollectMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityCollectService {

    private final ActivityCollectMapper activityCollectMapper;
    private final ActivityMapper activityMapper;

    /**
     * 收藏活动
     */
    @Transactional
    public void collectActivity(Long userId, Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        ActivityCollect existing = activityCollectMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已经收藏过该活动");
        }

        ActivityCollect collect = new ActivityCollect();
        collect.setUserId(userId);
        collect.setActivityId(activityId);
        activityCollectMapper.insert(collect);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void uncollectActivity(Long userId, Long activityId) {
        ActivityCollect existing = activityCollectMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "尚未收藏该活动");
        }

        activityCollectMapper.deleteByUserIdAndActivityId(userId, activityId);
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
