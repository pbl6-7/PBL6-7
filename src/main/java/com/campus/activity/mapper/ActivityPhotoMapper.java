package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityPhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityPhotoMapper {

    int insert(ActivityPhoto photo);

    ActivityPhoto selectById(@Param("id") Long id);

    List<ActivityPhoto> selectByActivityId(@Param("activityId") Long activityId);

    int deleteById(@Param("id") Long id);

    int deleteByActivityId(@Param("activityId") Long activityId);
}