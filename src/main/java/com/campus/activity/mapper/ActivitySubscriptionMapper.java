package com.campus.activity.mapper;

import com.campus.activity.entity.ActivitySubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 活动订阅Mapper接口
 * 提供活动订阅相关的数据访问方法
 */
@Mapper
public interface ActivitySubscriptionMapper {
    int insert(ActivitySubscription subscription);

    int deleteByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    ActivitySubscription selectByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    List<ActivitySubscription> selectByUserId(@Param("userId") Long userId);

    int countByActivityId(@Param("activityId") Long activityId);

    List<Long> selectUserIdsByActivityId(@Param("activityId") Long activityId);
}
