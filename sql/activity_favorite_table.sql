-- 活动收藏表
-- 用于存储用户收藏的活动信息
CREATE TABLE IF NOT EXISTS `activity_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `favorite_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_favorite_time` (`favorite_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动收藏表';
