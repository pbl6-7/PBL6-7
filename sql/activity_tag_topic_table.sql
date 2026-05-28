-- 活动标签表
-- 用于存储活动的标签信息
CREATE TABLE IF NOT EXISTS `activity_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#1890ff' COMMENT '标签颜色',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动标签表';

-- 活动标签关联表
-- 用于实现活动与标签的多对多关系
CREATE TABLE IF NOT EXISTS `activity_tag_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID，关联activity_tag表',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_tag_id` (`tag_id`),
    UNIQUE KEY `uk_activity_tag` (`activity_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动标签关联表';

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

-- 插入默认标签数据
INSERT INTO `activity_tag` (`name`, `color`) VALUES
('学术讲座', '#1890ff'),
('文艺演出', '#52c41a'),
('体育竞技', '#fa8c16'),
('志愿服务', '#eb2f96'),
('科技创新', '#13c2c2'),
('社会实践', '#722ed1'),
('社团活动', '#faad14'),
('职业发展', '#2f54eb');
