-- Create notifications table (idempotent)
SET @db = DATABASE();

SET @sql = (SELECT IF(COUNT(*) = 0,
  'CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT ''接收通知的用户'',
    type VARCHAR(32) NOT NULL COMMENT ''LIKE/MESSAGE/REPORT_HANDLED/BANNED/SYSTEM'',
    title VARCHAR(128) NOT NULL,
    content VARCHAR(500) NOT NULL,
    target_type VARCHAR(32) NULL COMMENT ''USER/DYNAMIC/REPORT/CHAT'',
    target_id VARCHAR(64) NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notif_user_unread (user_id, is_read),
    INDEX idx_notif_user_time   (user_id, created_at)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'SELECT 1')
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'notifications');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
