package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 活动图片Mapper接口
 */
@Mapper
public interface ActivityImageMapper {

    /**
     * 插入活动图片关联
     */
    @Insert("INSERT INTO activity_image (activity_id, file_id, display_order, created_at) " +
            "VALUES (#{activityId}, #{fileId}, #{displayOrder}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ActivityImage activityImage);

    /**
     * 根据活动ID查询图片列表
     */
    @Select("SELECT * FROM activity_image WHERE activity_id = #{activityId} ORDER BY display_order")
    List<ActivityImage> selectByActivityId(Long activityId);

    /**
     * 根据ID删除
     */
    @Delete("DELETE FROM activity_image WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 根据活动ID删除所有图片关联
     */
    @Delete("DELETE FROM activity_image WHERE activity_id = #{activityId}")
    void deleteByActivityId(Long activityId);
}
