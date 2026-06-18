-- 修复notification表缺少title列的问题
-- 执行此SQL前请先备份数据库

ALTER TABLE notification ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL DEFAULT '' AFTER activity_id;

-- 修复已有记录的title字段（从content中提取前50个字符作为标题）
UPDATE notification SET title = LEFT(content, 50) WHERE title = '' OR title IS NULL;
