package com.campus.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流拦截器
 * 基于令牌桶算法的接口限流实现
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    /**
     * 默认限流时间窗口（秒）
     */
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    /**
     * 默认最大请求数
     */
    private static final int DEFAULT_MAX_REQUESTS = 100;

    /**
     * 限流记录存储
     * key: 用户ID或IP, value: 请求计数和时间戳
     */
    private final Map<String, RateLimitRecord> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = getRateLimitKey(request);
        RateLimitRecord record = rateLimitMap.computeIfAbsent(key, k -> new RateLimitRecord());

        long currentTime = System.currentTimeMillis();

        // 检查是否在时间窗口内
        if (currentTime - record.windowStart > DEFAULT_WINDOW_SECONDS * 1000) {
            // 重置时间窗口
            record.windowStart = currentTime;
            record.count.set(0);
        }

        // 增加计数
        int count = record.count.incrementAndGet();

        // 检查是否超过限制
        if (count > DEFAULT_MAX_REQUESTS) {
            log.warn("请求限流触发: key={}, count={}, limit={}", key, count, DEFAULT_MAX_REQUESTS);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return false;
        }

        log.debug("限流检查通过: key={}, count={}/{}", key, count, DEFAULT_MAX_REQUESTS);
        return true;
    }

    /**
     * 获取限流键
     * 优先使用用户ID，否则使用IP
     */
    private String getRateLimitKey(HttpServletRequest request) {
        // 尝试从请求属性中获取用户ID
        Object userId = request.getAttribute("currentUserId");
        if (userId != null) {
            return "user:" + userId;
        }

        // 使用IP作为限流键
        String ip = getClientIP(request);
        return "ip:" + ip;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 限流记录内部类
     */
    private static class RateLimitRecord {
        /**
         * 时间窗口开始时间
         */
        volatile long windowStart = System.currentTimeMillis();

        /**
         * 请求计数
         */
        AtomicInteger count = new AtomicInteger(0);
    }
}
