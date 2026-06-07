package com.campus.activity.mapper;

import com.campus.activity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * @param typeId 活动类型ID
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
            @Param("typeId") Long typeId,
            @Param("location") String location,
            @Param("startTimeFrom") LocalDateTime startTimeFrom,
            @Param("startTimeTo") LocalDateTime startTimeTo,
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
     * @param typeId 活动类型ID
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
            @Param("typeId") Long typeId,
            @Param("location") String location,
            @Param("startTimeFrom") LocalDateTime startTimeFrom,
            @Param("startTimeTo") LocalDateTime startTimeTo
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

    /**
     * 批量插入活动
     * @param activities 活动列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<Activity> activities);

    /**
     * 游标分页查询活动列表（基于lastId）
     * @param lastId 上一次查询的最后一条记录ID（用于游标分页）
     * @param keyword 关键词
     * @param status 活动状态
     * @param approvalStatus 审核状态
     * @param publisherId 发布者ID
     * @param typeId 活动类型ID
     * @param location 活动地点
     * @param startTimeFrom 开始时间从
     * @param startTimeTo 开始时间至
     * @param size 每页数量
     * @return 活动列表
     */
    List<Activity> selectListByCursor(
            @Param("lastId") Long lastId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus,
            @Param("publisherId") Long publisherId,
            @Param("typeId") Long typeId,
            @Param("location") String location,
            @Param("startTimeFrom") LocalDateTime startTimeFrom,
            @Param("startTimeTo") LocalDateTime startTimeTo,
            @Param("size") Integer size
    );

    // ==================== 定时任务相关方法 ====================

    /**
     * 查询即将开始的活动（用于活动提醒定时任务）
     * @param startTimeFrom 开始时间范围起点
     * @param startTimeTo 开始时间范围终点
     * @return 活动列表
     */
    List<Activity> selectUpcomingActivities(
            @Param("startTimeFrom") LocalDateTime startTimeFrom,
            @Param("startTimeTo") LocalDateTime startTimeTo
    );

    /**
     * 查询已结束的活动（用于状态更新定时任务）
     * @param currentTime 当前时间
     * @return 已结束的活动列表
     */
    List<Activity> selectEndedActivities(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询待审核且超过指定时间的活动（用于自动取消定时任务）
     * @param days 超过天数
     * @return 待审核的活动列表
     */
    List<Activity> selectPendingActivitiesOverDays(@Param("days") Integer days);

    /**
     * 批量更新活动状态
     * @param ids 活动ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    // ==================== 统计相关方法 ====================

    /**
     * 按活动状态统计数量
     * @param status 活动状态
     * @return 数量
     */
    Long countByStatus(@Param("status") String status);

    /**
     * 按活动类型统计数量
     * @param typeId 活动类型ID
     * @return 数量
     */
    Long countByTypeId(@Param("typeId") Long typeId);

    /**
     * 统计指定时间范围内的活动数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数量
     */
    Long countByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按月统计活动数量
     * @param year 年份
     * @param month 月份
     * @return 数量
     */
    Long countByMonth(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 按周统计活动数量
     * @param year 年份
     * @param week 周数
     * @return 数量
     */
    Long countByWeek(@Param("year") Integer year, @Param("week") Integer week);

    /**
     * 查询热门活动（按报名人数排序）
     * @param limit 返回数量限制
     * @return 活动列表（包含报名人数）
     */
    List<Map<String, Object>> selectHotActivitiesByRegistration(@Param("limit") Integer limit);

    /**
     * 查询热门活动（按收藏人数排序）
     * @param limit 返回数量限制
     * @return 活动列表（包含收藏人数）
     */
    List<Map<String, Object>> selectHotActivitiesByCollection(@Param("limit") Integer limit);

    /**
     * 统计平均报名人数
     * @return 平均报名人数
     */
    Double selectAverageRegistrations();

    /**
     * 统计平均浏览次数
     * @return 平均浏览次数
     */
    Double selectAverageViewCount();

    /**
     * 按活动类型分组统计数量
     * @return 类型分布数据（typeId, count）
     */
    List<Map<String, Object>> selectTypeDistribution();

    /**
     * 按活动状态分组统计数量
     * @return 状态分布数据（status, count）
     */
    List<Map<String, Object>> selectStatusDistribution();

    /**
     * 按审核状态分组统计数量
     * @return 审核状态分布数据（approvalStatus, count）
     */
    List<Map<String, Object>> selectApprovalStatusDistribution();

    /**
     * 按月统计活动创建趋势
     * @param startMonth 开始月份（格式：YYYY-MM）
     * @param endMonth 结束月份（格式：YYYY-MM）
     * @return 月度趋势数据（month, count）
     */
    List<Map<String, Object>> selectMonthlyTrend(
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth
    );

    /**
     * 按周统计活动创建趋势
     * @param startWeek 开始周（格式：YYYY-W1）
     * @param endWeek 结束周（格式：YYYY-W1）
     * @return 周度趋势数据（week, count）
     */
    List<Map<String, Object>> selectWeeklyTrend(
            @Param("startWeek") String startWeek,
            @Param("endWeek") String endWeek
    );
}
