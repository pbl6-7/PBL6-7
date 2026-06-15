package com.campus.activity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 搜索性能监控服务
 */
@Slf4j
@Service
public class SearchPerformanceMonitor {

    private final Map<String, AtomicLong> searchCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> searchTimes = new ConcurrentHashMap<>();

    /**
     * 记录搜索耗时
     */
    public void recordSearchTime(String keyword, long timeMs) {
        searchCounts.computeIfAbsent(keyword, k -> new AtomicLong()).incrementAndGet();
        searchTimes.computeIfAbsent(keyword, k -> new AtomicLong()).addAndGet(timeMs);
    }

    /**
     * 获取搜索次数
     */
    public long getSearchCount(String keyword) {
        AtomicLong count = searchCounts.get(keyword);
        return count != null ? count.get() : 0;
    }

    /**
     * 获取平均搜索耗时
     */
    public double getAverageSearchTime(String keyword) {
        AtomicLong count = searchCounts.get(keyword);
        AtomicLong time = searchTimes.get(keyword);
        if (count == null || time == null || count.get() == 0) {
            return 0;
        }
        return (double) time.get() / count.get();
    }

    /**
     * 获取性能统计
     */
    public SearchStats getPerformanceStats() {
        SearchStats stats = new SearchStats();
        stats.totalSearches = searchCounts.values().stream().mapToLong(AtomicLong::get).sum();
        return stats;
    }

    /**
     * 获取慢查询列表
     */
    public java.util.List<SearchRecord> getSlowSearches(long thresholdMs) {
        return new java.util.ArrayList<>();
    }

    /**
     * 全局统计
     */
    public GlobalStats getGlobalStats() {
        GlobalStats stats = new GlobalStats();
        stats.totalSearches = searchCounts.values().stream().mapToLong(AtomicLong::get).sum();
        return stats;
    }

    /**
     * 性能摘要
     */
    public PerformanceSummary getPerformanceSummary() {
        return new PerformanceSummary();
    }

    /**
     * 清除统计
     */
    public void clearStats() {
        searchCounts.clear();
        searchTimes.clear();
    }

    /**
     * 记录搜索
     */
    public void recordSearch(String keyword, long timeMs, int resultCount, boolean hasResults, boolean isUserSearch, String searchType) {
        recordSearchTime(keyword, timeMs);
    }

    /**
     * 搜索统计内部类
     */
    public static class SearchStats {
        public long totalSearches;
        public double averageTime;
    }

    /**
     * 搜索记录内部类
     */
    public static class SearchRecord {
        public String keyword;
        public long searchTime;
        public long timestamp;
    }

    /**
     * 全局统计内部类
     */
    public static class GlobalStats {
        public long totalSearches;
        public double averageTime;
    }

    /**
     * 性能摘要内部类
     */
    public static class PerformanceSummary {
        public long totalSearches;
        public double averageTime;
        public long maxTime;
        public long minTime;
    }
}
