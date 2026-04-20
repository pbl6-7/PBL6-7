-- 活动表
-- 用于存储校园活动的相关信息
CREATE TABLE IF NOT EXISTS `activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `title` VARCHAR(200) NOT NULL COMMENT '活动名称',
    `publisher_id` BIGINT NOT NULL COMMENT '发布者ID，关联user_info表',
    `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
    `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
    `location` VARCHAR(200) NOT NULL COMMENT '活动地点',
    `description` TEXT COMMENT '活动描述',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '活动状态: draft-草稿, published-已发布, cancelled-已取消, ended-已结束',
    `approval_status` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态: pending-待审核, approved-已通过, rejected-已拒绝',
    `max_participants` INT DEFAULT 0 COMMENT '最大参与人数，0表示不限',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_status` (`status`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动信息表';
