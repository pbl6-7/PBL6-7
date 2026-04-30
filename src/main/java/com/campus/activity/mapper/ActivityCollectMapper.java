package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityCollectMapper {
    int insert(ActivityCollect collect);
    
    int deleteByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);
    
    ActivityCollect selectByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);
    
    List<ActivityCollect> selectByUserId(@Param("userId") Long userId);
    
    int countByActivityId(@Param("activityId") Long activityId);
}
