package com.campus.core.mapper;

import com.campus.core.entity.ActivityFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 活动收藏Mapper接口
 * 提供活动收藏相关的数据访问方法
 */
@Mapper
public interface ActivityFavoriteMapper {
    /**
     * 插入收藏记录
     * @param favorite 收藏实体
     * @return 影响行数
     */
    int insert(ActivityFavorite favorite);

    /**
     * 删除收藏记录
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 影响行数
     */
    int deleteByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * 查询用户的收藏记录
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 收藏记录
     */
    ActivityFavorite selectByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * 查询用户的所有收藏记录
     * @param userId 用户ID
     * @return 收藏记录列表
     */
    List<ActivityFavorite> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计活动的收藏数量
     * @param activityId 活动ID
     * @return 收藏数量
     */
    int countByActivityId(@Param("activityId") Long activityId);
}
