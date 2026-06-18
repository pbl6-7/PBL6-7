package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动标签Mapper接口
 * 提供标签数据的增删改查操作
 */
@Mapper
public interface ActivityTagMapper {

    /**
     * 插入标签
     * @param tag 标签实体
     * @return 插入的记录数
     */
    int insert(ActivityTag tag);

    /**
     * 批量插入标签
     * @param tags 标签列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("tags") List<ActivityTag> tags);

    /**
     * 根据活动ID删除标签
     * @param activityId 活动ID
     * @return 删除的记录数
     */
    int deleteByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据ID删除标签
     * @param id 标签ID
     * @return 删除的记录数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询标签
     * @param id 标签ID
     * @return 标签实体
     */
    ActivityTag selectById(@Param("id") Long id);

    /**
     * 根据名称查询标签
     * @param name 标签名称
     * @return 标签实体
     */
    ActivityTag selectByName(@Param("name") String name);

    /**
     * 查询所有标签
     * @return 标签列表
     */
    List<ActivityTag> selectAll();

    /**
     * 根据活动ID查询标签列表
     * @param activityId 活动ID
     * @return 标签列表
     */
    List<ActivityTag> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据多个活动ID查询标签列表
     * @param activityIds 活动ID列表
     * @return 标签列表
     */
    List<ActivityTag> selectByActivityIds(@Param("activityIds") List<Long> activityIds);

    /**
     * 删除活动与标签的关联关系
     * @param activityId 活动ID
     * @return 删除的记录数
     */
    int deleteRelationByActivityId(@Param("activityId") Long activityId);

    /**
     * 插入活动与标签的关联关系
     * @param activityId 活动ID
     * @param tagName 标签名称
     * @param color 标签颜色
     * @return 插入的记录数
     */
    int insertRelation(@Param("activityId") Long activityId, @Param("tagName") String tagName, @Param("color") String color);
}