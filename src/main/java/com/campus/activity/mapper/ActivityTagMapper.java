package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityTagMapper {
    int insert(ActivityTag tag);

    int batchInsert(@Param("tags") List<ActivityTag> tags);

    int deleteByActivityId(@Param("activityId") Long activityId);

    ActivityTag selectById(@Param("id") Long id);

    List<ActivityTag> selectByActivityId(@Param("activityId") Long activityId);

    List<ActivityTag> selectByActivityIds(@Param("activityIds") List<Long> activityIds);
}
