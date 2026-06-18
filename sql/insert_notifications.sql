-- 插入测试通知数据
INSERT INTO notification (user_id, activity_id, type, content, is_read, create_time) VALUES
(3, 1, 'registration', '您报名的人工智能前沿技术讲座活动即将开始', 0, NOW());

INSERT INTO notification (user_id, activity_id, type, content, is_read, create_time) VALUES
(3, 2, 'registration', '您报名的校园篮球比赛活动即将开始', 0, NOW());

INSERT INTO notification (user_id, activity_id, type, content, is_read, create_time) VALUES
(3, 5, 'activity', '您收藏的创新创业大赛有新动态', 0, NOW());

SELECT '通知数据插入成功' as result;
