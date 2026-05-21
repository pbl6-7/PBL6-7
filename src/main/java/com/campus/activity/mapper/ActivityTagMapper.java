package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityTagMapper {
    int insert(ActivityTag tag);

    int updateById(ActivityTag tag);

    int deleteById(@Param("id") Long id);

    ActivityTag selectById(@Param("id") Long id);

    ActivityTag selectByName(@Param("name") String name);

    List<ActivityTag> selectAll();

    List<ActivityTag> selectByActivityId(@Param("activityId") Long activityId);

    int insertRelation(@Param("activityId") Long activityId, @Param("tagId") Long tagId);

    int deleteRelationByActivityId(@Param("activityId") Long activityId);

    int deleteRelation(@Param("activityId") Long activityId, @Param("tagId") Long tagId);

    List<ActivityTag> selectByActivityIdAndTagIds(@Param("activityId") Long activityId, @Param("tagIds") List<Long> tagIds);
}
