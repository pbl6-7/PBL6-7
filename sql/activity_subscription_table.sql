-- 活动订阅表
-- 用于存储用户对活动的订阅关系
CREATE TABLE IF NOT EXISTS `activity_subscription` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动订阅表';
