-- 活动类型表
-- 用于存储活动的类型分类信息
CREATE TABLE IF NOT EXISTS `activity_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '类型ID',
    `name` VARCHAR(50) NOT NULL COMMENT '类型名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动类型表';

-- 插入默认活动类型数据
INSERT INTO `activity_type` (`name`) VALUES
('学术讲座'),
('文艺演出'),
('体育竞技'),
('志愿服务'),
('科技创新'),
('社会实践'),
('社团活动'),
('职业发展');
