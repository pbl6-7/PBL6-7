package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityTopicMapper {
    int insert(ActivityTopic topic);

    int updateById(ActivityTopic topic);

    int deleteById(@Param("id") Long id);

    ActivityTopic selectById(@Param("id") Long id);

    List<ActivityTopic> selectByActivityId(@Param("activityId") Long activityId);

    List<ActivityTopic> selectByCreatorId(@Param("creatorId") Long creatorId);

    List<ActivityTopic> selectAll();
}
