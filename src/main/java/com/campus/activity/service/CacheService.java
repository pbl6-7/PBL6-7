package com.campus.activity.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 * 基于Caffeine实现的高性能本地缓存，支持过期时间和大小限制
 */
@Slf4j
@Service
public class CacheService {

    /**
     * 默认缓存，最大1000条，写入后30分钟过期
     */
    private final Cache<String, Object> defaultCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 热门活动缓存，最大100条，写入后10分钟过期
     */
    private final Cache<String, Object> hotActivityCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 搜索建议缓存，最大500条，写入后30分钟过期
     */
    private final Cache<String, Object> searchSuggestionCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 用户信息缓存，最大200条，写入后15分钟过期
     */
    private final Cache<String, Object> userCache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, String subKey, Class<T> type) {
        String fullKey = buildKey(key, subKey);
        Cache<String, Object> cache = selectCache(key);
        Object value = cache.getIfPresent(fullKey);
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.warn("缓存类型转换失败: key={}", fullKey);
            cache.invalidate(fullKey);
            return null;
        }
    }

    /**
     * 设置缓存
     */
    public <T> void put(String key, String subKey, T value) {
        String fullKey = buildKey(key, subKey);
        Cache<String, Object> cache = selectCache(key);
        cache.put(fullKey, value);
    }

    /**
     * 设置缓存并指定过期时间
     */
    public <T> void put(String key, String subKey, T value, long timeout, TimeUnit unit) {
        // Caffeine不支持单条过期，使用对应缓存区域
        put(key, subKey, value);
    }

    /**
     * 清除指定前缀的缓存
     */
    public void clear(String key) {
        String prefix = key + ":";
        Cache<String, Object> cache = selectCache(key);
        cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * 清除所有缓存
     */
    public void clearAll() {
        defaultCache.invalidateAll();
        hotActivityCache.invalidateAll();
        searchSuggestionCache.invalidateAll();
        userCache.invalidateAll();
        log.info("所有缓存已清除");
    }

    /**
     * 获取搜索建议
     */
    public <T> T getSearchSuggestion(String keyword, Class<T> type, Runnable onMiss) {
        T result = get("search:suggestion", keyword, type);
        if (result == null && onMiss != null) {
            onMiss.run();
        }
        return result;
    }

    /**
     * 保存搜索建议
     */
    public void putSearchSuggestion(String keyword, Object result) {
        put("search:suggestion", keyword, result);
    }

    /**
     * 清除搜索建议缓存
     */
    public void evictSearchSuggestion(String keyword) {
        searchSuggestionCache.invalidate(buildKey("search:suggestion", keyword));
    }

    /**
     * 清除所有搜索建议缓存
     */
    public void clearSearchSuggestionCache() {
        searchSuggestionCache.invalidateAll();
    }

    /**
     * 获取热门活动
     */
    public <T> T getHotActivity(String key, Class<T> type, java.util.function.Supplier<T> onMiss) {
        T result = get(key, "", type);
        if (result == null) {
            result = onMiss.get();
        }
        return result;
    }

    /**
     * 驱逐热门活动缓存
     */
    public void evictHotActivity(String key) {
        hotActivityCache.invalidate(buildKey("hotActivity", key));
    }

    /**
     * 清除所有热门活动缓存
     */
    public void clearHotActivityCache() {
        hotActivityCache.invalidateAll();
    }

    /**
     * 驱逐匹配模式的缓存
     */
    public void evictByPattern(String pattern) {
        String prefix = pattern.replace("*", "");
        defaultCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        hotActivityCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        searchSuggestionCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        userCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * 获取缓存大小
     */
    public int size() {
        return (int) (defaultCache.estimatedSize() + hotActivityCache.estimatedSize()
                + searchSuggestionCache.estimatedSize() + userCache.estimatedSize());
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("defaultCache", getCacheStats(defaultCache, "default"));
        stats.put("hotActivityCache", getCacheStats(hotActivityCache, "hotActivity"));
        stats.put("searchSuggestionCache", getCacheStats(searchSuggestionCache, "searchSuggestion"));
        stats.put("userCache", getCacheStats(userCache, "user"));
        stats.put("totalSize", size());
        return stats;
    }

    /**
     * 获取单个缓存的统计信息
     */
    private Map<String, Object> getCacheStats(Cache<String, Object> cache, String name) {
        var stats = cache.stats();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("name", name);
        result.put("size", cache.estimatedSize());
        result.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
        result.put("hitCount", stats.hitCount());
        result.put("missCount", stats.missCount());
        result.put("evictionCount", stats.evictionCount());
        return result;
    }

    /**
     * 根据key前缀选择对应的缓存区域
     */
    private Cache<String, Object> selectCache(String key) {
        if (key == null) {
            return defaultCache;
        }
        if (key.startsWith("hotActivity") || key.startsWith("activity:share")) {
            return hotActivityCache;
        }
        if (key.startsWith("search:suggestion")) {
            return searchSuggestionCache;
        }
        if (key.startsWith("user:") || key.startsWith("users:")) {
            return userCache;
        }
        return defaultCache;
    }

    private String buildKey(String key, String subKey) {
        return key + ":" + subKey;
    }
}
