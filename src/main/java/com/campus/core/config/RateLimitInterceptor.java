package com.campus.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${rate-limit.max-requests:100}")
    private int maxRequests;

    /**
     * 最大限流记录数，防止内存泄漏
     */
    private static final int MAX_RECORD_SIZE = 10000;

    /**
     * 清理阈值，当记录数超过此值时触发清理
     */
    private static final int CLEANUP_THRESHOLD = 8000;

    /**
     * 限流记录存储
     * key: 用户ID或IP, value: 请求计数和时间戳
     */
    private final Map<String, RateLimitRecord> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 定期清理过期记录，防止内存泄漏
        cleanupExpiredRecords();

        String key = getRateLimitKey(request);
        RateLimitRecord record = rateLimitMap.computeIfAbsent(key, k -> new RateLimitRecord());

        long currentTime = System.currentTimeMillis();

        // 检查是否在时间窗口内
        if (currentTime - record.windowStart > windowSeconds * 1000L) {
            // 重置时间窗口
            record.windowStart = currentTime;
            record.count.set(0);
        }

        // 增加计数
        int count = record.count.incrementAndGet();

        // 检查是否超过限制
        if (count > maxRequests) {
            log.warn("请求限流触发: key={}, count={}, limit={}", key, count, maxRequests);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            return false;
        }

        log.debug("限流检查通过: key={}, count={}/{}", key, count, maxRequests);
        return true;
    }

    /**
     * 清理过期的限流记录，防止内存泄漏
     */
    private void cleanupExpiredRecords() {
        if (rateLimitMap.size() > CLEANUP_THRESHOLD) {
            long expireTime = System.currentTimeMillis() - windowSeconds * 1000L;
            rateLimitMap.entrySet().removeIf(entry -> entry.getValue().windowStart < expireTime);
            // 如果清理后仍然超过最大限制，移除最早的记录
            if (rateLimitMap.size() > MAX_RECORD_SIZE) {
                int toRemove = rateLimitMap.size() - CLEANUP_THRESHOLD;
                var iterator = rateLimitMap.keySet().iterator();
                while (iterator.hasNext() && toRemove > 0) {
                    iterator.next();
                    iterator.remove();
                    toRemove--;
                }
            }
        }
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
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 取第一个IP（最接近客户端的IP）
            int commaIndex = ip.indexOf(',');
            if (commaIndex > 0) {
                ip = ip.substring(0, commaIndex).trim();
            }
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
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
