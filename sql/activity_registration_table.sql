-- 活动报名表
-- 用于存储用户报名参加活动的信息
CREATE TABLE IF NOT EXISTS `activity_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `registration_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '报名状态: pending-待确认, confirmed-已确认, cancelled-已取消',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';
