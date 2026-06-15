package com.campus.core.service;

import com.campus.activity.entity.Activity;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.entity.ActivityFavorite;
import com.campus.core.mapper.ActivityFavoriteMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏服务类
 * 提供活动收藏相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private static final String APPROVAL_STATUS_APPROVED = "approved";
    private static final String STATUS_PUBLISHED = "published";

    private final ActivityFavoriteMapper activityFavoriteMapper;
    private final ActivityMapper activityMapper;

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param activityId 活动ID
     */
    @Transactional
    public void addFavorite(Long userId, Long activityId) {
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

        ActivityFavorite existing = activityFavoriteMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已经收藏过该活动");
        }

        ActivityFavorite favorite = new ActivityFavorite();
        favorite.setUserId(userId);
        favorite.setActivityId(activityId);
        favorite.setCreatedAt(LocalDateTime.now());
        activityFavoriteMapper.insert(favorite);

        log.info("用户 {} 成功收藏活动 {}", userId, activityId);
    }

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param activityId 活动ID
     */
    @Transactional
    public void removeFavorite(Long userId, Long activityId) {
        log.info("用户 {} 开始取消收藏活动 {}", userId, activityId);

        ActivityFavorite existing = activityFavoriteMapper.selectByUserIdAndActivityId(userId, activityId);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "尚未收藏该活动");
        }

        activityFavoriteMapper.deleteByUserIdAndActivityId(userId, activityId);

        log.info("用户 {} 成功取消收藏活动 {}", userId, activityId);
    }

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Long userId, Long activityId) {
        return activityFavoriteMapper.selectByUserIdAndActivityId(userId, activityId) != null;
    }

    /**
     * 获取活动收藏数
     * @param activityId 活动ID
     * @return 收藏数量
     */
    public int getFavoriteCount(Long activityId) {
        return activityFavoriteMapper.countByActivityId(activityId);
    }

    /**
     * 获取用户收藏列表（包含活动详情）
     * @param userId 用户ID
     * @return 收藏详情列表
     */
    public List<Map<String, Object>> getUserFavorites(Long userId) {
        List<ActivityFavorite> favorites = activityFavoriteMapper.selectByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ActivityFavorite favorite : favorites) {
            Activity activity = activityMapper.selectById(favorite.getActivityId());
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteId", favorite.getId());
            item.put("activityId", favorite.getActivityId());
            item.put("activityTitle", activity != null ? activity.getTitle() : "未知活动");
            item.put("activityLocation", activity != null ? activity.getLocation() : "未知地点");
            item.put("startTime", activity != null ? activity.getStartTime() : null);
            item.put("favoriteTime", favorite.getCreatedAt());
            result.add(item);
        }

        return result;
    }
}
