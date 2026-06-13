package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityTypeMapper {
    /**
     * 插入活动类型
     * @param activityType 活动类型
     * @return 插入的记录数
     */
    int insert(ActivityType activityType);

    /**
     * 根据ID查询活动类型
     * @param id 类型ID
     * @return 活动类型
     */
    ActivityType selectById(@Param("id") Long id);

    /**
     * 根据名称查询活动类型
     * @param name 类型名称
     * @return 活动类型
     */
    ActivityType selectByName(@Param("name") String name);

    /**
     * 查询所有活动类型
     * @return 活动类型列表
     */
    List<ActivityType> selectAll();

    /**
     * 更新活动类型
     * @param activityType 活动类型
     * @return 更新的记录数
     */
    int updateById(ActivityType activityType);

    /**
     * 删除活动类型
     * @param id 类型ID
     * @return 删除的记录数
     */
    int deleteById(@Param("id") Long id);
}
