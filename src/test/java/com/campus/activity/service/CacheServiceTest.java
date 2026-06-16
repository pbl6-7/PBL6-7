package com.campus.activity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存服务单元测试
 */
class CacheServiceTest {

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService();
    }

    @Test
    void testPutAndGet() {
        cacheService.put("test", "key1", "value1");
        String result = cacheService.get("test", "key1", String.class);
        assertEquals("value1", result);
    }

    @Test
    void testGet_NotFound() {
        String result = cacheService.get("test", "nonexistent", String.class);
        assertNull(result);
    }

    @Test
    void testPutInteger() {
        cacheService.put("count", "activity1", 42);
        Integer result = cacheService.get("count", "activity1", Integer.class);
        assertEquals(42, result);
    }

    @Test
    void testClear() {
        cacheService.put("test", "key1", "value1");
        cacheService.put("test", "key2", "value2");
        cacheService.clear("test");
        assertNull(cacheService.get("test", "key1", String.class));
        assertNull(cacheService.get("test", "key2", String.class));
    }

    @Test
    void testClearAll() {
        cacheService.put("test", "key1", "value1");
        cacheService.put("hotActivity", "key2", "value2");
        cacheService.clearAll();
        assertNull(cacheService.get("test", "key1", String.class));
        assertNull(cacheService.get("hotActivity", "key2", String.class));
    }

    @Test
    void testSize() {
        assertEquals(0, cacheService.size());
        cacheService.put("test", "key1", "value1");
        assertTrue(cacheService.size() > 0);
    }

    @Test
    void testGetStats() {
        cacheService.put("test", "key1", "value1");
        Map<String, Object> stats = cacheService.getStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalSize"));
        assertTrue(stats.containsKey("defaultCache"));
    }

    @Test
    void testSearchSuggestionCache() {
        cacheService.putSearchSuggestion("keyword:test", "result1");
        Object result = cacheService.getSearchSuggestion("keyword:test", String.class, null);
        assertEquals("result1", result);
    }

    @Test
    void testHotActivityCache() {
        cacheService.put("hotActivity", "activity1", "热门活动");
        String result = cacheService.get("hotActivity", "activity1", String.class);
        assertEquals("热门活动", result);
    }

    @Test
    void testEvictByPattern() {
        cacheService.put("test", "key1", "value1");
        cacheService.put("test", "key2", "value2");
        cacheService.evictByPattern("test*");
        assertNull(cacheService.get("test", "key1", String.class));
    }
}
