-- 活动标签表
-- 每个标签与唯一的活动关联，活动删除时标签级联删除
CREATE TABLE IF NOT EXISTS `activity_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#1890ff' COMMENT '标签颜色',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动标签表';

-- 活动话题表
-- 用于存储活动的话题信息
CREATE TABLE IF NOT EXISTS `activity_topic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '话题ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `title` VARCHAR(200) NOT NULL COMMENT '话题标题',
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID，关联user_info表',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动话题表';
