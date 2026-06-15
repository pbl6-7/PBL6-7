package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityAlbum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityAlbumMapper {
    List<ActivityAlbum> selectByActivityId(@Param("activityId") Long activityId);
    ActivityAlbum selectById(@Param("id") Long id);
    int insert(ActivityAlbum album);
    int deleteById(@Param("id") Long id);
    int deleteByActivityId(@Param("activityId") Long activityId);
    int countByActivityId(@Param("activityId") Long activityId);
}
