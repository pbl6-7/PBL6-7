-- 敏感词表
CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
    `is_whitelist` TINYINT DEFAULT 0 COMMENT '是否白名单(1-是, 0-否)',
    `type` VARCHAR(20) DEFAULT 'other' COMMENT '敏感词类型(politics-政治, violence-暴力, porn-色情, other-其他)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- 插入默认敏感词
INSERT IGNORE INTO `sensitive_word` (`word`, `is_whitelist`, `type`) VALUES
('暴力', 0, 'violence'),
('色情', 0, 'porn'),
('赌博', 0, 'other'),
('毒品', 0, 'other'),
('诈骗', 0, 'other');
