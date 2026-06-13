package com.campus.user.mapper;

import com.campus.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户
     */ 
    User selectByUsername(@Param("username") String username);
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户
     */ 
    User selectById(@Param("id") Long id);
    /**
     * 批量查询用户
     * @param ids 用户ID列表
     * @return 用户列表
     */
    List<User> selectBatchIds(@Param("ids") List<Long> ids);
    /**
     * 插入用户
     * @param user 用户
     * @return 插入的用户ID
     */ 
    int insert(User user);
    /**
     * 更新用户
     * @param user 用户
     * @return 更新的用户ID
     */ 
    int updateById(User user);
    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除的用户ID
     */ 
    int deleteById(@Param("id") Long id);
    /**
     * 查询所有用户（管理员功能）
     * @return 用户列表
     */
    List<User> selectAllUsers();
    /**
     * 按角色查询用户
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> selectUsersByRole(@Param("role") String role);
    /**
     * 分页查询用户
     * @param keyword 关键词
     * @param role 角色
     * @param offset 偏移量
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> selectUserPageList(
        @Param("keyword") String keyword,
        @Param("role") String role,
        @Param("offset") Integer offset,
        @Param("size") Integer size
    );
    /**
     * 统计用户总数
     * @param keyword 关键词
     * @param role 角色
     * @return 用户总数
     */
    Long countUsers(
        @Param("keyword") String keyword,
        @Param("role") String role
    );
    /**
     * 更新用户角色
     * @param id 用户ID
     * @param role 新角色
     * @return 更新的记录数
     */
    int updateUserRole(@Param("id") Long id, @Param("role") String role);

    /**
     * 查询所有用户ID列表
     * @return 用户ID列表
     */
    List<Long> selectAllIds();

    /**
     * 统计所有用户数量
     * @return 用户总数
     */
    Long countAllUsers();

    /**
     * 按角色统计用户数量
     * @param role 用户角色
     * @return 用户数量
     */
    Long countUsersByRole(@Param("role") String role);

    /**
     * 查询最近创建的用户（管理员功能）
     * @param limit 返回数量限制
     * @return 用户列表
     */
    List<User> selectRecentUsers(@Param("limit") Integer limit);

    /**
     * 批量插入用户
     * @param users 用户列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<User> users);

    /**
     * 游标分页查询用户列表（基于lastId）
     * @param lastId 上一次查询的最后一条记录ID（用于游标分页）
     * @param keyword 关键词
     * @param role 角色
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> selectUserPageListByCursor(
        @Param("lastId") Long lastId,
        @Param("keyword") String keyword,
        @Param("role") String role,
        @Param("size") Integer size
    );

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 用户状态
     * @return 更新的记录数
     */
    int updateUserStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 查询禁用用户列表
     * @param keyword 关键词
     * @param offset 偏移量
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> selectDisabledUsers(
        @Param("keyword") String keyword,
        @Param("offset") Integer offset,
        @Param("size") Integer size
    );

    /**
     * 统计禁用用户数量
     * @param keyword 关键词
     * @return 用户数量
     */
    Long countDisabledUsers(@Param("keyword") String keyword);

    /**
     * 批量更新用户状态
     * @param ids 用户ID列表
     * @param status 用户状态
     * @return 更新的记录数
     */
    int batchUpdateUserStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除的记录数
     */
    int batchDeleteUsers(@Param("ids") List<Long> ids);

    /**
     * 批量更新用户角色
     * @param ids 用户ID列表
     * @param role 新角色
     * @return 更新的记录数
     */
    int batchUpdateUserRole(@Param("ids") List<Long> ids, @Param("role") String role);

    // ==================== 统计相关方法 ====================

    /**
     * 统计指定时间范围内注册的用户数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数量
     */
    Long countByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按月统计用户注册数量
     * @param year 年份
     * @param month 月份
     * @return 数量
     */
    Long countByMonth(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 按周统计用户注册数量
     * @param year 年份
     * @param week 周数
     * @return 数量
     */
    Long countByWeek(@Param("year") Integer year, @Param("week") Integer week);

    /**
     * 统计活跃用户数量（最近登录）
     * @param days 最近多少天内登录
     * @return 活跃用户数量
     */
    Long countActiveUsers(@Param("days") Integer days);

    /**
     * 统计最近注册的用户数量
     * @param days 最近多少天内注册
     * @return 用户数量
     */
    Long countRecentUsers(@Param("days") Integer days);

    /**
     * 统计不活跃用户数量
     * @param days 多少天未登录算不活跃
     * @return 不活跃用户数量
     */
    Long countInactiveUsers(@Param("days") Integer days);

    /**
     * 按角色分组统计用户数量
     * @return 角色分布数据（role, count）
     */
    List<Map<String, Object>> selectRoleDistribution();

    /**
     * 按月统计用户注册趋势
     * @param startMonth 开始月份（格式：YYYY-MM）
     * @param endMonth 结束月份（格式：YYYY-MM）
     * @return 月度趋势数据（month, count）
     */
    List<Map<String, Object>> selectMonthlyRegistrationTrend(
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth
    );

    /**
     * 按周统计用户注册趋势
     * @param startWeek 开始周（格式：YYYY-W1）
     * @param endWeek 结束周（格式：YYYY-W1）
     * @return 周度趋势数据（week, count）
     */
    List<Map<String, Object>> selectWeeklyRegistrationTrend(
            @Param("startWeek") String startWeek,
            @Param("endWeek") String endWeek
    );

    /**
     * 按天统计用户注册趋势
     * @param startDate 开始日期（格式：YYYY-MM-DD）
     * @param endDate 结束日期（格式：YYYY-MM-DD）
     * @return 日度趋势数据（day, count）
     */
    List<Map<String, Object>> selectDailyRegistrationTrend(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 统计平均每人报名活动数量
     * @return 平均报名数量
     */
    Double selectAverageRegistrationsPerUser();

    /**
     * 统计用户报名数量分布
     * @return 分布数据（range, count）
     */
    List<Map<String, Object>> selectUserRegistrationRangeDistribution();
}
