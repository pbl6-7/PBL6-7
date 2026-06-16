package com.campus.activity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 搜索性能监控服务
 * 记录搜索耗时、统计性能指标、检测慢查询
 */
@Slf4j
@Service
public class SearchPerformanceMonitor {

    /**
     * 最大记录数，防止内存溢出
     */
    private static final int MAX_RECORDS = 1000;

    private final Map<String, AtomicLong> searchCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> searchTimes = new ConcurrentHashMap<>();
    private final Queue<SearchRecord> slowSearchRecords = new ConcurrentLinkedQueue<>();
    private final AtomicLong totalSearchTime = new AtomicLong();
    private final AtomicLong totalSearchCount = new AtomicLong();
    private final AtomicLong maxSearchTime = new AtomicLong();
    private final AtomicLong minSearchTime = new AtomicLong(Long.MAX_VALUE);

    /**
     * 记录搜索耗时
     */
    public void recordSearchTime(String keyword, long timeMs) {
        searchCounts.computeIfAbsent(keyword, k -> new AtomicLong()).incrementAndGet();
        searchTimes.computeIfAbsent(keyword, k -> new AtomicLong()).addAndGet(timeMs);
        totalSearchCount.incrementAndGet();
        totalSearchTime.addAndGet(timeMs);

        // 更新最大最小值
        long currentMax;
        do {
            currentMax = maxSearchTime.get();
            if (timeMs <= currentMax) break;
        } while (!maxSearchTime.compareAndSet(currentMax, timeMs));

        long currentMin;
        do {
            currentMin = minSearchTime.get();
            if (timeMs >= currentMin) break;
        } while (!minSearchTime.compareAndSet(currentMin, timeMs));

        // 记录慢查询（超过500ms）
        if (timeMs > 500) {
            SearchRecord record = new SearchRecord();
            record.keyword = keyword;
            record.searchTime = timeMs;
            record.timestamp = System.currentTimeMillis();
            slowSearchRecords.add(record);
            // 限制记录数量
            while (slowSearchRecords.size() > MAX_RECORDS) {
                slowSearchRecords.poll();
            }
        }
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
        stats.totalSearches = totalSearchCount.get();
        stats.averageTime = totalSearchCount.get() > 0
                ? (double) totalSearchTime.get() / totalSearchCount.get()
                : 0;
        return stats;
    }

    /**
     * 获取慢查询列表
     */
    public List<SearchRecord> getSlowSearches(long thresholdMs) {
        return slowSearchRecords.stream()
                .filter(r -> r.searchTime >= thresholdMs)
                .sorted((a, b) -> Long.compare(b.searchTime, a.searchTime))
                .limit(50)
                .collect(Collectors.toList());
    }

    /**
     * 全局统计
     */
    public GlobalStats getGlobalStats() {
        GlobalStats stats = new GlobalStats();
        stats.totalSearches = totalSearchCount.get();
        stats.averageTime = totalSearchCount.get() > 0
                ? (double) totalSearchTime.get() / totalSearchCount.get()
                : 0;
        return stats;
    }

    /**
     * 性能摘要
     */
    public PerformanceSummary getPerformanceSummary() {
        PerformanceSummary summary = new PerformanceSummary();
        summary.totalSearches = totalSearchCount.get();
        summary.averageTime = totalSearchCount.get() > 0
                ? (double) totalSearchTime.get() / totalSearchCount.get()
                : 0;
        summary.maxTime = maxSearchTime.get();
        summary.minTime = totalSearchCount.get() > 0 ? minSearchTime.get() : 0;
        return summary;
    }

    /**
     * 清除统计
     */
    public void clearStats() {
        searchCounts.clear();
        searchTimes.clear();
        slowSearchRecords.clear();
        totalSearchTime.set(0);
        totalSearchCount.set(0);
        maxSearchTime.set(0);
        minSearchTime.set(Long.MAX_VALUE);
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
