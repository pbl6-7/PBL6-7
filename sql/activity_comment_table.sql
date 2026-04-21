-- 活动评论表
-- 用于存储用户对活动的评论信息
CREATE TABLE IF NOT EXISTS `activity_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `comment_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_comment_time` (`comment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动评论表';
