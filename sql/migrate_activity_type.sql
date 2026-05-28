-- 数据迁移脚本：将 activity 表的 activity_type 字段值迁移到 activity_type 表
-- 执行前请确保 activity_type 表已创建并初始化了基础类型数据

-- 步骤1：将唯一的 activity_type 值插入到 activity_type 表中（如果不存在）
INSERT IGNORE INTO `activity_type` (`name`)
SELECT DISTINCT activity_type
FROM activity
WHERE activity_type IS NOT NULL AND activity_type != '';

-- 步骤2：为 activity 表添加 type_id 字段（如果表结构尚未修改）
-- ALTER TABLE `activity` ADD COLUMN `type_id` BIGINT DEFAULT NULL COMMENT '活动类型ID，关联activity_type表' AFTER `approval_status`;

-- 步骤3：根据 activity_type 名称更新 type_id
UPDATE activity a
INNER JOIN activity_type t ON a.activity_type = t.name
SET a.type_id = t.id
WHERE a.activity_type IS NOT NULL AND a.activity_type != '';

-- 步骤4：删除 activity 表的 activity_type 字段（表结构修改后执行）
-- ALTER TABLE `activity` DROP COLUMN `activity_type`;
