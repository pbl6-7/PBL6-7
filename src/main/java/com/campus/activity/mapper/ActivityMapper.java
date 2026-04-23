package com.campus.activity.mapper;

import com.campus.activity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityMapper {
    /**
     * 插入活动
     * @param activity 活动
     * @return 插入的记录数
     */
    int insert(Activity activity);

    /**
     * 根据ID查询活动
     * @param id 活动ID
     * @return 活动
     */
    Activity selectById(@Param("id") Long id);

    /**
     * 查询用户发布的所有活动
     * @param publisherId 发布者ID
     * @return 活动列表
     */
    List<Activity> selectByPublisherId(@Param("publisherId") Long publisherId);

    /**
     * 更新活动
     * @param activity 活动
     * @return 更新的记录数
     */
    int updateById(Activity activity);

    /**
     * 删除活动
     * @param id 活动ID
     * @return 删除的记录数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询活动列表（带筛选和分页）
     * @param publisherId 发布者ID
     * @param status 活动状态
     * @param approvalStatus 审核状态
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param offset 偏移量
     * @param size 每页数量
     * @return 活动列表
     */
    List<Activity> selectList(
            @Param("publisherId") Long publisherId,
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 查询活动总数（带筛选条件）
     * @param publisherId 发布者ID
     * @param status 活动状态
     * @param approvalStatus 审核状态
     * @return 活动总数
     */
    Long count(
            @Param("publisherId") Long publisherId,
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus
    );
}
