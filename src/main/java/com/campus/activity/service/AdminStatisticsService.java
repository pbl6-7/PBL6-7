package com.campus.activity.service;

import com.campus.activity.dto.*;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.activity.mapper.ActivityCollectMapper;
import com.campus.activity.mapper.ActivityTypeMapper;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员数据统计服务
 * 提供系统整体统计、活动统计、用户统计、报名统计等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final ActivityCollectMapper collectMapper;
    private final ActivityTypeMapper activityTypeMapper;
    private final UserMapper userMapper;
    private final CacheService cacheService;
    private final StatisticsAsyncService statisticsAsyncService;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "statistics:";
    private static final String OVERVIEW_CACHE_KEY = CACHE_PREFIX + "overview";
    private static final String ACTIVITY_CACHE_KEY = CACHE_PREFIX + "activities";
    private static final String USER_CACHE_KEY = CACHE_PREFIX + "users";
    private static final String REGISTRATION_CACHE_KEY = CACHE_PREFIX + "registrations";

    /**
     * 获取系统概览统计
     * 包含活动、用户、报名的整体统计数据
     * 
     * @return 系统概览统计数据
     */
    public OverviewStatisticsDTO getOverviewStatistics() {
        log.info("获取系统概览统计");
        
        // 尝试从缓存获取
        OverviewStatisticsDTO cachedStats = cacheService.get(
                "statisticsCache", 
                OVERVIEW_CACHE_KEY, 
                OverviewStatisticsDTO.class
        );
        
        if (cachedStats != null) {
            log.info("从缓存获取概览统计数据");
            return cachedStats;
        }
        
        // 计算统计数据
        OverviewStatisticsDTO statistics = calculateOverviewStatistics();
        
        // 缓存结果（缓存5分钟）
        cacheService.put("statisticsCache", OVERVIEW_CACHE_KEY, statistics);
        
        return statistics;
    }

    /**
     * 获取活动统计
     * 包含活动总数、状态分布、类型分布、趋势数据等
     * 
     * @return 活动统计数据
     */
    public ActivityStatisticsDTO getActivityStatistics() {
        log.info("获取活动统计");
        
        // 尝试从缓存获取
        ActivityStatisticsDTO cachedStats = cacheService.get(
                "statisticsCache", 
                ACTIVITY_CACHE_KEY, 
                ActivityStatisticsDTO.class
        );
        
        if (cachedStats != null) {
            log.info("从缓存获取活动统计数据");
            return cachedStats;
        }
        
        // 计算统计数据
        ActivityStatisticsDTO statistics = calculateActivityStatistics();
        
        // 缓存结果（缓存5分钟）
        cacheService.put("statisticsCache", ACTIVITY_CACHE_KEY, statistics);
        
        return statistics;
    }

    /**
     * 获取用户统计
     * 包含用户总数、角色分布、注册趋势、活跃用户等
     * 
     * @return 用户统计数据
     */
    public UserStatisticsDTO getUserStatistics() {
        log.info("获取用户统计");
        
        // 尝试从缓存获取
        UserStatisticsDTO cachedStats = cacheService.get(
                "statisticsCache", 
                USER_CACHE_KEY, 
                UserStatisticsDTO.class
        );
        
        if (cachedStats != null) {
            log.info("从缓存获取用户统计数据");
            return cachedStats;
        }
        
        // 计算统计数据
        UserStatisticsDTO statistics = calculateUserStatistics();
        
        // 缓存结果（缓存5分钟）
        cacheService.put("statisticsCache", USER_CACHE_KEY, statistics);
        
        return statistics;
    }

    /**
     * 获取报名统计
     * 包含报名总数、状态分布、趋势数据、热门活动等
     * 
     * @return 报名统计数据
     */
    public RegistrationStatisticsDTO getRegistrationStatistics() {
        log.info("获取报名统计");
        
        // 尝试从缓存获取
        RegistrationStatisticsDTO cachedStats = cacheService.get(
                "statisticsCache", 
                REGISTRATION_CACHE_KEY, 
                RegistrationStatisticsDTO.class
        );
        
        if (cachedStats != null) {
            log.info("从缓存获取报名统计数据");
            return cachedStats;
        }
        
        // 计算统计数据
        RegistrationStatisticsDTO statistics = calculateRegistrationStatistics();
        
        // 缓存结果（缓存5分钟）
        cacheService.put("statisticsCache", REGISTRATION_CACHE_KEY, statistics);
        
        return statistics;
    }

    /**
     * 获取趋势统计（按时间）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param timeUnit 时间单位（month、week、day）
     * @return 趋势统计数据
     */
    public Map<String, List<TrendDataDTO>> getTrendStatistics(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            String timeUnit) {
        
        log.info("获取趋势统计: startDate={}, endDate={}, timeUnit={}", startDate, endDate, timeUnit);
        
        Map<String, List<TrendDataDTO>> trends = new HashMap<>();
        
        // 活动趋势
        trends.put("activities", calculateActivityTrend(startDate, endDate, timeUnit));
        
        // 用户注册趋势
        trends.put("users", calculateUserRegistrationTrend(startDate, endDate, timeUnit));
        
        // 报名趋势
        trends.put("registrations", calculateRegistrationTrend(startDate, endDate, timeUnit));
        
        return trends;
    }

    /**
     * 获取热门活动统计
     * 
     * @param limit 返回数量限制
     * @param sortBy 排序方式（registration、collection、view）
     * @return 热门活动列表
     */
    public List<HotActivityDTO> getHotActivities(Integer limit, String sortBy) {
        log.info("获取热门活动统计: limit={}, sortBy={}", limit, sortBy);
        
        String cacheKey = CACHE_PREFIX + "hot:" + sortBy + ":" + limit;
        
        // 尝试从缓存获取
        List<HotActivityDTO> cachedActivities = cacheService.get(
                "statisticsCache", 
                cacheKey, 
                List.class
        );
        
        if (cachedActivities != null) {
            log.info("从缓存获取热门活动数据");
            return cachedActivities;
        }
        
        // 计算热门活动
        List<HotActivityDTO> hotActivities = calculateHotActivities(limit, sortBy);
        
        // 缓存结果（缓存10分钟）
        cacheService.put("statisticsCache", cacheKey, hotActivities);
        
        return hotActivities;
    }

    /**
     * 清除统计缓存
     */
    public void clearStatisticsCache() {
        log.info("清除统计缓存");
        cacheService.clear("statisticsCache");
    }

    // ==================== 内部计算方法 ====================

    /**
     * 计算系统概览统计
     */
    private OverviewStatisticsDTO calculateOverviewStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        // 基础统计
        Long totalActivities = activityMapper.countAll();
        Long totalUsers = userMapper.countAllUsers();
        Long totalRegistrations = registrationMapper.countAll();
        Long pendingActivities = activityMapper.countByApprovalStatus("pending");
        
        // 时间范围统计
        Long newActivities7Days = activityMapper.countByTimeRange(sevenDaysAgo, now);
        Long newUsers7Days = userMapper.countByTimeRange(sevenDaysAgo, now);
        Long newRegistrations7Days = registrationMapper.countByTimeRange(sevenDaysAgo, now);
        
        Long newActivities30Days = activityMapper.countByTimeRange(thirtyDaysAgo, now);
        Long newUsers30Days = userMapper.countByTimeRange(thirtyDaysAgo, now);
        Long newRegistrations30Days = registrationMapper.countByTimeRange(thirtyDaysAgo, now);
        
        // 活跃用户统计
        Long activeUsers = userMapper.countActiveUsers(30);
        
        // 计算系统健康度评分（示例算法）
        Double systemHealthScore = calculateSystemHealthScore(
                totalActivities, totalUsers, totalRegistrations, activeUsers
        );
        
        return OverviewStatisticsDTO.builder()
                .totalActivities(totalActivities)
                .totalUsers(totalUsers)
                .totalRegistrations(totalRegistrations)
                .pendingActivities(pendingActivities)
                .newActivities7Days(newActivities7Days)
                .newUsers7Days(newUsers7Days)
                .newRegistrations7Days(newRegistrations7Days)
                .newActivities30Days(newActivities30Days)
                .newUsers30Days(newUsers30Days)
                .newRegistrations30Days(newRegistrations30Days)
                .activeUsers(activeUsers)
                .systemHealthScore(systemHealthScore)
                .updateTime(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    /**
     * 计算活动统计
     */
    private ActivityStatisticsDTO calculateActivityStatistics() {
        // 活动总数
        Long totalActivities = activityMapper.countAll();
        
        // 状态分布
        Map<String, Long> statusDistribution = convertListToMap(
                activityMapper.selectStatusDistribution()
        );
        
        // 审核状态分布
        Map<String, Long> approvalStatusDistribution = convertListToMap(
                activityMapper.selectApprovalStatusDistribution()
        );
        
        // 类型分布
        Map<String, Long> typeDistribution = convertListToMap(
                activityMapper.selectTypeDistribution()
        );
        
        return ActivityStatisticsDTO.builder()
                .totalActivities(totalActivities)
                .publishedActivities(statusDistribution.getOrDefault("published", 0L))
                .draftActivities(statusDistribution.getOrDefault("draft", 0L))
                .cancelledActivities(statusDistribution.getOrDefault("cancelled", 0L))
                .statusDistribution(statusDistribution)
                .approvalStatusDistribution(approvalStatusDistribution)
                .typeDistribution(typeDistribution)
                .build();
    }

    /**
     * 计算用户统计
     */
    private UserStatisticsDTO calculateUserStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        // 用户总数
        Long totalUsers = userMapper.countAllUsers();
        
        // 角色分布
        Map<String, Long> roleDistribution = convertListToMap(
                userMapper.selectRoleDistribution()
        );
        
        // 月度注册趋势（最近12个月）
        String startMonth = now.minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String endMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<TrendDataDTO> monthlyRegistrationTrend = convertTrendList(
                userMapper.selectMonthlyRegistrationTrend(startMonth, endMonth)
        );
        
        // 周度注册趋势（最近12周）
        String startWeek = calculateWeekString(now.minusWeeks(12));
        String endWeek = calculateWeekString(now);
        List<TrendDataDTO> weeklyRegistrationTrend = convertTrendList(
                userMapper.selectWeeklyRegistrationTrend(startWeek, endWeek)
        );
        
        // 活跃用户统计
        Long activeUsers = userMapper.countActiveUsers(30);
        Long inactiveUsers = userMapper.countInactiveUsers(30);
        
        // 用户报名数量分布
        Map<String, Long> registrationDistribution = convertListToMap(
                userMapper.selectUserRegistrationRangeDistribution()
        );
        
        // 平均报名数量
        Double averageRegistrationsPerUser = userMapper.selectAverageRegistrationsPerUser();
        
        // 新增用户统计
        Long newUsers7Days = userMapper.countByTimeRange(sevenDaysAgo, now);
        Long newUsers30Days = userMapper.countByTimeRange(thirtyDaysAgo, now);
        
        return UserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .roleDistribution(roleDistribution)
                .monthlyRegistrationTrend(monthlyRegistrationTrend)
                .weeklyRegistrationTrend(weeklyRegistrationTrend)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .registrationDistribution(registrationDistribution)
                .averageRegistrationsPerUser(averageRegistrationsPerUser != null ? averageRegistrationsPerUser : 0.0)
                .newUsers7Days(newUsers7Days)
                .newUsers30Days(newUsers30Days)
                .build();
    }

    /**
     * 计算报名统计
     */
    private RegistrationStatisticsDTO calculateRegistrationStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        // 报名总数
        Long totalRegistrations = registrationMapper.countAll();
        
        // 状态分布
        Map<String, Long> statusDistribution = convertListToMap(
                registrationMapper.selectStatusDistribution()
        );
        
        // 月度趋势（最近12个月）
        String startMonth = now.minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String endMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<TrendDataDTO> monthlyTrend = convertTrendList(
                registrationMapper.selectMonthlyTrend(startMonth, endMonth)
        );
        
        // 周度趋势（最近12周）
        String startWeek = calculateWeekString(now.minusWeeks(12));
        String endWeek = calculateWeekString(now);
        List<TrendDataDTO> weeklyTrend = convertTrendList(
                registrationMapper.selectWeeklyTrend(startWeek, endWeek)
        );
        
        // 热门活动报名数量
        List<Map<String, Object>> hotActivityData = registrationMapper.selectHotActivityRegistrations(10);
        Map<Long, Long> hotActivityRegistrations = new HashMap<>();
        for (Map<String, Object> data : hotActivityData) {
            Long activityId = toLong(data.get("activityId"));
            Long count = toLong(data.get("count"));
            hotActivityRegistrations.put(activityId, count);
        }

        // 用户报名数量分布
        List<Map<String, Object>> userRegistrationData = registrationMapper.selectUserRegistrationDistribution();
        Map<Long, Long> userRegistrationCount = new HashMap<>();
        for (Map<String, Object> data : userRegistrationData) {
            Long userId = toLong(data.get("userId"));
            Long count = toLong(data.get("count"));
            userRegistrationCount.put(userId, count);
        }
        
        // 平均值统计
        Double averageRegistrationsPerActivity = registrationMapper.selectAverageRegistrationsPerActivity();
        Double confirmationRate = registrationMapper.selectConfirmationRate();
        
        // 时间范围统计
        Long registrations7Days = registrationMapper.countByTimeRange(sevenDaysAgo, now);
        Long registrations30Days = registrationMapper.countByTimeRange(thirtyDaysAgo, now);
        
        return RegistrationStatisticsDTO.builder()
                .totalRegistrations(totalRegistrations)
                .statusDistribution(statusDistribution)
                .monthlyTrend(monthlyTrend)
                .weeklyTrend(weeklyTrend)
                .hotActivityRegistrations(hotActivityRegistrations)
                .userRegistrationCount(userRegistrationCount)
                .averageRegistrationsPerActivity(averageRegistrationsPerActivity != null ? averageRegistrationsPerActivity : 0.0)
                .registrations7Days(registrations7Days)
                .registrations30Days(registrations30Days)
                .confirmationRate(confirmationRate != null ? confirmationRate : 0.0)
                .build();
    }

    /**
     * 计算活动趋势
     */
    private List<TrendDataDTO> calculateActivityTrend(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            String timeUnit) {
        
        List<Map<String, Object>> trendData;
        
        if ("month".equals(timeUnit)) {
            String startMonth = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String endMonth = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            trendData = activityMapper.selectMonthlyTrend(startMonth, endMonth);
        } else if ("week".equals(timeUnit)) {
            String startWeek = calculateWeekString(startDate);
            String endWeek = calculateWeekString(endDate);
            trendData = activityMapper.selectWeeklyTrend(startWeek, endWeek);
        } else if ("day".equals(timeUnit)) {
            String startDay = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDay = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            trendData = activityMapper.selectDailyTrend(startDay, endDay);
        } else {
            trendData = new ArrayList<>();
        }
        
        return convertTrendList(trendData);
    }

    /**
     * 计算用户注册趋势
     */
    private List<TrendDataDTO> calculateUserRegistrationTrend(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            String timeUnit) {
        
        List<Map<String, Object>> trendData;
        
        if ("month".equals(timeUnit)) {
            String startMonth = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String endMonth = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            trendData = userMapper.selectMonthlyRegistrationTrend(startMonth, endMonth);
        } else if ("week".equals(timeUnit)) {
            String startWeek = calculateWeekString(startDate);
            String endWeek = calculateWeekString(endDate);
            trendData = userMapper.selectWeeklyRegistrationTrend(startWeek, endWeek);
        } else if ("day".equals(timeUnit)) {
            String startDay = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDay = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            trendData = userMapper.selectDailyRegistrationTrend(startDay, endDay);
        } else {
            trendData = new ArrayList<>();
        }
        
        return convertTrendList(trendData);
    }

    /**
     * 计算报名趋势
     */
    private List<TrendDataDTO> calculateRegistrationTrend(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            String timeUnit) {
        
        List<Map<String, Object>> trendData;
        
        if ("month".equals(timeUnit)) {
            String startMonth = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String endMonth = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            trendData = registrationMapper.selectMonthlyTrend(startMonth, endMonth);
        } else if ("week".equals(timeUnit)) {
            String startWeek = calculateWeekString(startDate);
            String endWeek = calculateWeekString(endDate);
            trendData = registrationMapper.selectWeeklyTrend(startWeek, endWeek);
        } else if ("day".equals(timeUnit)) {
            String startDay = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDay = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            trendData = registrationMapper.selectDailyTrend(startDay, endDay);
        } else {
            trendData = new ArrayList<>();
        }
        
        return convertTrendList(trendData);
    }

    /**
     * 计算热门活动
     */
    private List<HotActivityDTO> calculateHotActivities(Integer limit, String sortBy) {
        List<Map<String, Object>> activityData;
        
        if ("registration".equals(sortBy)) {
            activityData = activityMapper.selectHotActivitiesByRegistration(limit);
        } else if ("collection".equals(sortBy)) {
            activityData = activityMapper.selectHotActivitiesByCollection(limit);
        } else {
            activityData = activityMapper.selectHotActivitiesByRegistration(limit);
        }
        
        List<HotActivityDTO> hotActivities = new ArrayList<>();
        for (Map<String, Object> data : activityData) {
            HotActivityDTO hotActivity = HotActivityDTO.builder()
                    .activityId(toLong(data.get("activityId")))
                    .title(toStr(data.get("title")))
                    .activityType(toStr(data.get("activityType")))
                    .registrationCount(toLong(data.getOrDefault("registrationCount", 0)))
                    .collectionCount(toLong(data.getOrDefault("collectionCount", 0)))
                    .viewCount(toLong(data.getOrDefault("viewCount", 0)))
                    .status(toStr(data.get("status")))
                    .startTime(parseDateTime(toStr(data.get("startTime"))))
                    .location(toStr(data.get("location")))
                    .hotScore(calculateHotScore(data))
                    .build();
            hotActivities.add(hotActivity);
        }
        
        return hotActivities;
    }

    /**
     * 计算系统健康度评分
     */
    private Double calculateSystemHealthScore(
            Long totalActivities, 
            Long totalUsers, 
            Long totalRegistrations, 
            Long activeUsers) {
        
        // 示例算法：综合考虑活动数量、用户活跃度、报名数量
        double activityScore = Math.min(totalActivities / 100.0, 1.0) * 30;
        double userScore = Math.min(totalUsers / 1000.0, 1.0) * 20;
        double registrationScore = Math.min(totalRegistrations / 500.0, 1.0) * 30;
        double activeUserScore = (activeUsers > 0 ? (double) activeUsers / totalUsers : 0) * 20;
        
        return activityScore + userScore + registrationScore + activeUserScore;
    }

    /**
     * 计算热度评分
     */
    private Double calculateHotScore(Map<String, Object> data) {
        Long registrationCount = toLong(data.getOrDefault("registrationCount", 0));
        Long collectionCount = toLong(data.getOrDefault("collectionCount", 0));
        Long viewCount = toLong(data.getOrDefault("viewCount", 0));
        
        // 权重：报名50%，收藏30%，浏览20%
        return registrationCount * 0.5 + collectionCount * 0.3 + viewCount * 0.002;
    }

    /**
     * 计算周字符串（格式：YYYY-Www）
     * 使用Java标准库的ISO周计算
     */
    private String calculateWeekString(LocalDateTime date) {
        WeekFields weekFields = WeekFields.ISO;
        int year = date.getYear();
        int week = date.get(weekFields.weekOfWeekBasedYear());
        return String.format("%d-W%02d", year, week);
    }

    /**
     * 将List<Map>转换为Map<String, Long>
     */
    private Map<String, Long> convertListToMap(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        
        return list.stream()
                .collect(Collectors.toMap(
                        map -> String.valueOf(map.get("key")),
                        map -> toLong(map.get("value")),
                        (v1, v2) -> v1,
                        HashMap::new
                ));
    }

    /**
     * 将趋势数据List转换为TrendDataDTO列表
     */
    private List<TrendDataDTO> convertTrendList(List<Map<String, Object>> trendData) {
        if (trendData == null || trendData.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TrendDataDTO> trends = new ArrayList<>();
        Long cumulativeValue = 0L;
        
        for (int i = 0; i < trendData.size(); i++) {
            Map<String, Object> data = trendData.get(i);
            Long value = toLong(data.get("value"));
            cumulativeValue += value;

            // 计算增长率
            Double growthRate = null;
            if (i > 0) {
                Long previousValue = toLong(trendData.get(i - 1).get("value"));
                if (previousValue > 0) {
                    growthRate = ((double) (value - previousValue) / previousValue) * 100;
                }
            }
            
            TrendDataDTO trend = TrendDataDTO.builder()
                    .label(toStr(data.get("label")))
                    .value(value)
                    .growthRate(growthRate)
                    .cumulativeValue(cumulativeValue)
                    .build();
            
            trends.add(trend);
        }

        return trends;
    }

    /**
     * 安全地将Number对象转换为Long
     * MySQL的COUNT(*)返回Integer，直接强转Long会抛ClassCastException
     * @param obj 原始对象
     * @return Long值
     */
    private Long toLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 安全地将对象转换为String
     * 数据库返回的LocalDateTime等类型不能直接强转String
     * @param obj 原始对象
     * @return String值，null返回null
     */
    private String toStr(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * 安全地解析日期时间字符串
     * 支持多种格式：ISO格式(含T)、标准格式(空格)、LocalDateTime.toString()输出
     *
     * @param dateStr 日期时间字符串
     * @return LocalDateTime对象，解析失败返回null
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 尝试ISO格式（含T分隔符）
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e1) {
            try {
                // 尝试标准格式（空格分隔符）
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e2) {
                try {
                    // 尝试直接解析（LocalDateTime.toString()输出格式）
                    return LocalDateTime.parse(dateStr);
                } catch (Exception e3) {
                    log.warn("无法解析日期时间: {}", dateStr);
                    return null;
                }
            }
        }
    }

    // ==================== 旧方法保留（兼容性） ====================

    /**
     * 获取活动统计（旧方法，返回Map）
     * @deprecated 使用 getActivityStatistics() 替代
     */
    @Deprecated
    public Map<String, Object> getActivityStatisticsOld() {
        ActivityStatisticsDTO dto = getActivityStatistics();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", dto.getTotalActivities());
        statistics.put("statusDistribution", dto.getStatusDistribution());
        statistics.put("approvalStatusDistribution", dto.getApprovalStatusDistribution());
        return statistics;
    }

    /**
     * 获取用户统计（旧方法，返回Map）
     * @deprecated 使用 getUserStatistics() 替代
     */
    @Deprecated
    public Map<String, Object> getUserStatisticsOld() {
        UserStatisticsDTO dto = getUserStatistics();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", dto.getTotalUsers());
        statistics.put("roleDistribution", dto.getRoleDistribution());
        return statistics;
    }

    /**
     * 获取报名统计（旧方法，返回Map）
     * @deprecated 使用 getRegistrationStatistics() 替代
     */
    @Deprecated
    public Map<String, Object> getRegistrationStatisticsOld() {
        RegistrationStatisticsDTO dto = getRegistrationStatistics();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRegistrations", dto.getTotalRegistrations());
        statistics.put("statusDistribution", dto.getStatusDistribution());
        return statistics;
    }

    /**
     * 获取每日统计（旧方法）
     * @deprecated 使用 getOverviewStatistics() 替代
     */
    @Deprecated
    public Map<String, Object> getDailyStatistics() {
        OverviewStatisticsDTO dto = getOverviewStatistics();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("activitiesThisWeek", dto.getNewActivities7Days());
        statistics.put("registrationsThisWeek", dto.getNewRegistrations7Days());
        statistics.put("newUsersThisWeek", dto.getNewUsers7Days());
        return statistics;
    }
}