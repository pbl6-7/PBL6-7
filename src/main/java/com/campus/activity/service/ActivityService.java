package com.campus.activity.service;

import com.campus.activity.config.CacheConfig;
import com.campus.activity.entity.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 活动服务类
 * 
 * 提供活动相关的业务逻辑，包括缓存支持：
 * - 热门活动缓存
 * - 活动详情缓存
 * - 缓存自动失效
 */
@Slf4j
@Service
public class ActivityService {

    @Autowired
    private CacheService cacheService;

    // 模拟数据访问层（实际项目中应该注入Mapper）
    // @Autowired
    // private ActivityMapper activityMapper;

    /**
     * 获取热门活动列表（带缓存）
     * 
     * 缓存键格式：hotActivity:list:{limit}
     * 
     * @param limit 限制数量
     * @return 热门活动列表
     */
    public List<Activity> getHotActivities(int limit) {
        String cacheKey = String.format("hotActivity:list:%d", limit);
        
        return cacheService.getHotActivity(cacheKey, List.class, () -> {
            log.info("从数据库获取热门活动列表，limit={}", limit);
            // 实际项目中应该调用：activityMapper.selectHotActivities(limit)
            // 这里返回模拟数据
            return List.of(
                createMockActivity(1L, "热门活动1"),
                createMockActivity(2L, "热门活动2"),
                createMockActivity(3L, "热门活动3")
            );
        });
    }

    /**
     * 根据ID获取活动详情（带缓存）
     * 
     * 缓存键格式：hotActivity:detail:{id}
     * 
     * @param id 活动ID
     * @return 活动详情
     */
    public Optional<Activity> getActivityById(Long id) {
        String cacheKey = String.format("hotActivity:detail:%d", id);
        
        Activity activity = cacheService.getHotActivity(cacheKey, Activity.class, () -> {
            log.info("从数据库获取活动详情，id={}", id);
            // 实际项目中应该调用：activityMapper.selectById(id)
            // 这里返回模拟数据
            return createMockActivity(id, "活动" + id);
        });
        
        return Optional.ofNullable(activity);
    }

    /**
     * 创建活动（自动清除相关缓存）
     * 
     * @param activity 活动信息
     * @return 创建后的活动
     */
    public Activity createActivity(Activity activity) {
        log.info("创建活动：{}", activity.getTitle());
        
        // 实际项目中应该调用：activityMapper.insert(activity)
        activity.setId(System.currentTimeMillis());
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        
        // 清除热门活动列表缓存
        cacheService.evictHotActivity("hotActivity:list:*");
        log.info("已清除热门活动列表缓存");
        
        return activity;
    }

    /**
     * 更新活动（自动清除缓存）
     * 
     * @param activity 活动信息
     * @return 更新后的活动
     */
    public Activity updateActivity(Activity activity) {
        log.info("更新活动：id={}", activity.getId());
        
        // 实际项目中应该调用：activityMapper.update(activity)
        activity.setUpdateTime(LocalDateTime.now());
        
        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", activity.getId());
        cacheService.evictHotActivity(cacheKey);
        log.info("已清除活动详情缓存：{}", cacheKey);
        
        // 清除热门活动列表缓存
        cacheService.evictHotActivity("hotActivity:list:*");
        log.info("已清除热门活动列表缓存");
        
        return activity;
    }

    /**
     * 删除活动（自动清除缓存）
     * 
     * @param id 活动ID
     */
    public void deleteActivity(Long id) {
        log.info("删除活动：id={}", id);
        
        // 实际项目中应该调用：activityMapper.deleteById(id)
        
        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        log.info("已清除活动详情缓存：{}", cacheKey);
        
        // 清除热门活动列表缓存
        cacheService.evictHotActivity("hotActivity:list:*");
        log.info("已清除热门活动列表缓存");
    }

    /**
     * 更新活动状态（自动清除缓存）
     * 
     * @param id 活动ID
     * @param status 新状态
     */
    public void updateActivityStatus(Long id, Integer status) {
        log.info("更新活动状态：id={}, status={}", id, status);
        
        // 实际项目中应该调用：activityMapper.updateStatus(id, status)
        
        // 清除活动详情缓存
        String cacheKey = String.format("hotActivity:detail:%d", id);
        cacheService.evictHotActivity(cacheKey);
        log.info("已清除活动详情缓存：{}", cacheKey);
        
        // 清除热门活动列表缓存
        cacheService.evictHotActivity("hotActivity:list:*");
        log.info("已清除热门活动列表缓存");
    }

    /**
     * 创建模拟活动对象
     */
    private Activity createMockActivity(Long id, String title) {
        return Activity.builder()
                .id(id)
                .title(title)
                .description("这是一个精彩的活动")
                .location("校园广场")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .status(1)
                .creatorId(1L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .viewCount(100)
                .participantCount(50)
                .build();
    }
}