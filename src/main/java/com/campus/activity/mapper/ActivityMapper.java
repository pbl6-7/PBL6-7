package com.campus.activity.mapper;

import com.campus.activity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
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
     * 查询活动列表（带多维度筛选和分页）
     * @param publisherId 发布者ID
     * @param keyword 关键词
     * @param status 活动状态
     * @param approvalStatus 审核状态
     * @param activityType 活动类型
     * @param location 活动地点
     * @param startTimeFrom 开始时间从
     * @param startTimeTo 开始时间至
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param offset 偏移量
     * @param size 每页数量
     * @return 活动列表
     */
    List<Activity> selectList(
            @Param("publisherId") Long publisherId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus,
            @Param("activityType") String activityType,
            @Param("location") String location,
            @Param("startTimeFrom") LocalDateTime startTimeFrom,
            @Param("startTimeTo") LocalDateTime startTimeTo,
            @Param("tagId") Long tagId,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 查询活动总数（带多维度筛选条件）
     * @param publisherId 发布者ID
     * @param keyword 关键词
     * @param status 活动状态
     * @param approvalStatus 审核状态
     * @param activityType 活动类型
     * @param location 活动地点
     * @param startTimeFrom 开始时间从
     * @param startTimeTo 开始时间至
     * @return 活动总数
     */
    Long count(
            @Param("publisherId") Long publisherId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus,
            @Param("activityType") String activityType,
            @Param("location") String location,
            @Param("startTimeFrom") LocalDateTime startTimeFrom,
            @Param("startTimeTo") LocalDateTime startTimeTo,
            @Param("tagId") Long tagId
    );

    List<Activity> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询待审核活动列表
     * @return 待审核活动列表
     */
    List<Activity> selectPendingActivities();

    /**
     * 按审核状态查询活动
     * @param approvalStatus 审核状态
     * @return 活动列表
     */
    List<Activity> selectByApprovalStatus(@Param("approvalStatus") String approvalStatus);

    /**
     * 统计各审核状态的数量
     * @param approvalStatus 审核状态
     * @return 数量
     */
    Long countByApprovalStatus(@Param("approvalStatus") String approvalStatus);

    /**
     * 统计总活动数
     * @return 总数
     */
    Long countAll();

    /**
     * 查询最近的注册用户数
     * @param days 天数
     * @return 用户数
     */
    Long countRecentUsers(@Param("days") Integer days);

    /**
     * 统计指定天数后创建的活动数
     * @param days 天数
     * @return 活动数
     */
    Long countCreatedAfterDays(@Param("days") Integer days);

    /**
     * 查询最近创建的活动
     * @param limit 返回数量限制
     * @return 活动列表
     */
    List<Activity> selectRecentActivities(@Param("limit") Integer limit);
}
