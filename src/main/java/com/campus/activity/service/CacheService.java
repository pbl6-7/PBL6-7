package com.campus.activity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 */
@Slf4j
@Service
public class CacheService {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        Object value;
        long expireTime;

        CacheEntry(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return expireTime > 0 && System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 获取缓存
     */
    public <T> T get(String key, String subKey, Class<T> type) {
        String fullKey = buildKey(key, subKey);
        CacheEntry entry = cache.get(fullKey);
        if (entry == null || entry.isExpired()) {
            return null;
        }
        return type.cast(entry.value);
    }

    /**
     * 设置缓存
     */
    public <T> void put(String key, String subKey, T value) {
        String fullKey = buildKey(key, subKey);
        cache.put(fullKey, new CacheEntry(value, 0));
    }

    /**
     * 设置缓存并指定过期时间
     */
    public <T> void put(String key, String subKey, T value, long timeout, TimeUnit unit) {
        String fullKey = buildKey(key, subKey);
        cache.put(fullKey, new CacheEntry(value, System.currentTimeMillis() + unit.toMillis(timeout)));
    }

    /**
     * 清除缓存
     */
    public void clear(String key) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(key + ":"));
    }

    /**
     * 清除所有缓存
     */
    public void clearAll() {
        cache.clear();
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
        put("search:suggestion", keyword, result, 30, TimeUnit.MINUTES);
    }

    /**
     * 清除搜索建议缓存
     */
    public void evictSearchSuggestion(String keyword) {
        clear("search:suggestion:" + keyword);
    }

    /**
     * 清除所有搜索建议缓存
     */
    public void clearSearchSuggestionCache() {
        clear("search:suggestion");
    }

    private String buildKey(String key, String subKey) {
        return key + ":" + subKey;
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
        clear("hotActivity:" + key);
    }

    /**
     * 清除所有热门活动缓存
     */
    public void clearHotActivityCache() {
        clear("hotActivity");
    }

    /**
     * 驱逐匹配模式的缓存
     */
    public void evictByPattern(String pattern) {
        // 移除前缀匹配的所有键
        String prefix = pattern.replace("*", "");
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }
}
