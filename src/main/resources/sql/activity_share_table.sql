-- 活动分享记录表
CREATE TABLE IF NOT EXISTS `activity_share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id` BIGINT NOT NULL COMMENT '分享用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动分享记录表';
