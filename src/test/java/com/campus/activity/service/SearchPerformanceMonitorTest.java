package com.campus.activity.service;

import com.campus.activity.service.SearchPerformanceMonitor.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索性能监控单元测试
 */
class SearchPerformanceMonitorTest {

    private SearchPerformanceMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new SearchPerformanceMonitor();
    }

    @Test
    void testRecordSearchTime() {
        monitor.recordSearchTime("篮球", 100);
        assertEquals(1, monitor.getSearchCount("篮球"));
    }

    @Test
    void testAverageSearchTime() {
        monitor.recordSearchTime("篮球", 100);
        monitor.recordSearchTime("篮球", 200);
        double avg = monitor.getAverageSearchTime("篮球");
        assertEquals(150.0, avg, 0.01);
    }

    @Test
    void testPerformanceSummary() {
        monitor.recordSearchTime("篮球", 100);
        monitor.recordSearchTime("足球", 200);
        PerformanceSummary summary = monitor.getPerformanceSummary();
        assertEquals(2, summary.totalSearches);
        assertEquals(150.0, summary.averageTime, 0.01);
        assertEquals(200, summary.maxTime);
        assertEquals(100, summary.minTime);
    }

    @Test
    void testSlowSearches() {
        monitor.recordSearchTime("慢查询1", 600);
        monitor.recordSearchTime("快查询", 50);
        monitor.recordSearchTime("慢查询2", 800);

        List<SearchRecord> slowSearches = monitor.getSlowSearches(500);
        assertEquals(2, slowSearches.size());
        assertTrue(slowSearches.get(0).searchTime >= slowSearches.get(1).searchTime);
    }

    @Test
    void testGlobalStats() {
        monitor.recordSearchTime("篮球", 100);
        monitor.recordSearchTime("足球", 300);
        GlobalStats stats = monitor.getGlobalStats();
        assertEquals(2, stats.totalSearches);
        assertEquals(200.0, stats.averageTime, 0.01);
    }

    @Test
    void testClearStats() {
        monitor.recordSearchTime("篮球", 100);
        monitor.clearStats();
        assertEquals(0, monitor.getSearchCount("篮球"));
        PerformanceSummary summary = monitor.getPerformanceSummary();
        assertEquals(0, summary.totalSearches);
    }

    @Test
    void testRecordSearch() {
        monitor.recordSearch("篮球", 100, 10, true, true, "activity");
        assertEquals(1, monitor.getSearchCount("篮球"));
    }
}
