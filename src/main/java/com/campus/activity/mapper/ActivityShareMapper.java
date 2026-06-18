package com.campus.activity.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 活动分享Mapper接口
 * 提供活动分享记录的数据库操作
 */
@Mapper
public interface ActivityShareMapper {

    /**
     * 记录分享行为
     */
    void insertShare(@Param("activityId") Long activityId, @Param("userId") Long userId);

    /**
     * 获取活动分享次数
     */
    Long countByActivityId(@Param("activityId") Long activityId);

    /**
     * 检查用户是否已分享过该活动
     */
    Integer checkUserShared(@Param("activityId") Long activityId, @Param("userId") Long userId);
}
