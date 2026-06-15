package com.campus.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 * 用于读取 application.yml 中的自定义配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    /**
     * 文件上传配置
     */
    private FileUpload fileUpload = new FileUpload();

    /**
     * 活动配置
     */
    private ActivityConfig activity = new ActivityConfig();

    /**
     * 评论配置
     */
    private CommentConfig comment = new CommentConfig();

    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 文件上传配置
     */
    @Data
    public static class FileUpload {
        /**
         * 最大文件大小（MB）
         */
        private int maxSize = 5;

        /**
         * 允许的文件类型
         */
        private String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "pdf"};

        /**
         * 上传路径
         */
        private String uploadPath = "/uploads";
    }

    /**
     * 活动配置
     */
    @Data
    public static class ActivityConfig {
        /**
         * 最大标签数
         */
        private int maxTags = 10;

        /**
         * 最大参与人数
         */
        private int maxParticipants = 100000;

        /**
         * 热门活动缓存时间（秒）
         */
        private int hotActivityCacheSeconds = 300;
    }

    /**
     * 评论配置
     */
    @Data
    public static class CommentConfig {
        /**
         * 最大评论长度
         */
        private int maxLength = 500;

        /**
         * 最大回复深度
         */
        private int maxReplyDepth = 3;

        /**
         * 是否启用敏感词过滤
         */
        private boolean sensitiveWordFilterEnabled = true;

        /**
         * 获取最大评论长度
         */
        public int getMaxContentLength() {
            return maxLength;
        }

        /**
         * 获取最大回复深度
         */
        public int getMaxReplyDepth() {
            return maxReplyDepth;
        }
    }

    /**
     * 获取分页配置
     */
    public PaginationConfig getPagination() {
        return new PaginationConfig();
    }

    /**
     * 分页配置
     */
    @Data
    public static class PaginationConfig {
        private int defaultPageSize = 20;
        private int maxPageSize = 100;
    }

    /**
     * 缓存配置
     */
    @Data
    public static class CacheConfig {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;

        /**
         * 默认过期时间（秒）
         */
        private int defaultTtlSeconds = 3600;
    }
}
