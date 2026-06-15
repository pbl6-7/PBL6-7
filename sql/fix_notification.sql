-- 修复通知表结构
ALTER TABLE notification 
ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL AFTER id,
ADD INDEX idx_user_id (user_id),
ADD INDEX idx_is_read (is_read);

-- 插入测试通知数据
INSERT INTO notification (user_id, title, content, type, is_read, created_at) VALUES 
(3, '您有一个新活动邀请', '您报名的"人工智能前沿技术讲座"即将开始', 'registration', FALSE, NOW()),
(3, '活动审核通过', '您发布的活动已审核通过', 'activity', FALSE, NOW()),
(3, '收藏活动更新', '您收藏的"校园篮球比赛"有新动态', 'favorite', TRUE, NOW());

-- 验证通知表结构
DESCRIBE notification;
