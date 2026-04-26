-- Create user_blacklist table (idempotent)
SET @db = DATABASE();

SET @sql = (SELECT IF(COUNT(*) = 0,
  'CREATE TABLE user_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT ''发起拉黑的用户'',
    blocked_user_id BIGINT NOT NULL COMMENT ''被拉黑的用户'',
    reason VARCHAR(255) NULL COMMENT ''拉黑原因'',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_blocked (user_id, blocked_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_blocked_user_id (blocked_user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'SELECT 1')
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_blacklist');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
