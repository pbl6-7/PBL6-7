-- 通知表
-- 用于存储用户收到的通知信息
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型: SUBSCRIPTION_STATUS-订阅状态变更, ACTIVITY_UPDATE-活动更新, ACTIVITY_START-活动开始, ACTIVITY_END-活动结束',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
