package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityRegistrationMapper {

    int insert(ActivityRegistration registration);

    int updateById(ActivityRegistration registration);

    int deleteById(@Param("id") Long id);

    int deleteByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

    ActivityRegistration selectById(@Param("id") Long id);

    ActivityRegistration selectByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

    List<ActivityRegistration> selectByUserId(@Param("userId") Long userId);

    List<ActivityRegistration> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 分页查询活动的报名记录
     * @param activityId 活动ID
     * @param offset 偏移量
     * @param size 每页数量
     * @return 报名记录列表
     */
    List<ActivityRegistration> selectByActivityIdWithPage(
            @Param("activityId") Long activityId,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    List<ActivityRegistration> selectByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") String status);

    Long countByActivityId(@Param("activityId") Long activityId);

    Long countByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") String status);

    List<ActivityRegistration> selectByUserIdWithPage(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("size") Integer size);

    Long countByUserId(@Param("userId") Long userId);

    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    Long countAll();

    Long countRecentRegistrations(@Param("days") Integer days);

    // ==================== 统计相关方法 ====================

    /**
     * 按报名状态统计数量
     * @param status 报名状态
     * @return 数量
     */
    Long countByStatus(@Param("status") String status);

    /**
     * 统计指定时间范围内的报名数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数量
     */
    Long countByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按月统计报名数量
     * @param year 年份
     * @param month 月份
     * @return 数量
     */
    Long countByMonth(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 按周统计报名数量
     * @param year 年份
     * @param week 周数
     * @return 数量
     */
    Long countByWeek(@Param("year") Integer year, @Param("week") Integer week);

    /**
     * 查询热门活动的报名数量
     * @param limit 返回数量限制
     * @return 活动报名数量列表（activityId, count）
     */
    List<Map<String, Object>> selectHotActivityRegistrations(@Param("limit") Integer limit);

    /**
     * 统计用户的报名活动数量
     * @param userId 用户ID
     * @return 报名数量
     */
    Long countUserRegistrations(@Param("userId") Long userId);

    /**
     * 统计平均每个活动的报名人数
     * @return 平均报名人数
     */
    Double selectAverageRegistrationsPerActivity();

    /**
     * 按报名状态分组统计数量
     * @return 状态分布数据（status, count）
     */
    List<Map<String, Object>> selectStatusDistribution();

    /**
     * 按月统计报名趋势
     * @param startMonth 开始月份（格式：YYYY-MM）
     * @param endMonth 结束月份（格式：YYYY-MM）
     * @return 月度趋势数据（month, count）
     */
    List<Map<String, Object>> selectMonthlyTrend(
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth
    );

    /**
     * 按周统计报名趋势
     * @param startWeek 开始周（格式：YYYY-W1）
     * @param endWeek 结束周（格式：YYYY-W1）
     * @return 周度趋势数据（week, count）
     */
    List<Map<String, Object>> selectWeeklyTrend(
            @Param("startWeek") String startWeek,
            @Param("endWeek") String endWeek
    );

    /**
     * 按天统计报名趋势
     * @param startDate 开始日期（格式：YYYY-MM-DD）
     * @param endDate 结束日期（格式：YYYY-MM-DD）
     * @return 日度趋势数据（day, count）
     */
    List<Map<String, Object>> selectDailyTrend(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 统计报名成功率
     * @return 成功率（已确认报名数/总报名数）
     */
    Double selectConfirmationRate();

    /**
     * 统计用户报名数量分布
     * @return 用户报名分布数据（userId, count）
     */
    List<Map<String, Object>> selectUserRegistrationDistribution();

    /**
     * 获取活动的已报名用户ID列表
     */
    List<Long> selectRegisteredUserIdsByActivityId(@Param("activityId") Long activityId);
}
