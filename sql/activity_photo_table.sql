CREATE TABLE IF NOT EXISTS activity_photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '照片ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    photo_url VARCHAR(500) NOT NULL COMMENT '照片URL',
    photo_name VARCHAR(255) COMMENT '照片名称',
    uploaded_by BIGINT NOT NULL COMMENT '上传者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_activity_id (activity_id),
    INDEX idx_uploaded_by (uploaded_by),
    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动相册表';