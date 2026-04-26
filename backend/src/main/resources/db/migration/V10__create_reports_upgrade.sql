-- Upgrade reports table with new fields (idempotent)
SET @db = DATABASE();

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE reports ADD COLUMN target_type VARCHAR(32) NULL COMMENT ''USER/MESSAGE/DYNAMIC''',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'reports' AND COLUMN_NAME = 'target_type');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE reports ADD COLUMN target_id VARCHAR(64) NULL COMMENT ''消息ID或动态ID''',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'reports' AND COLUMN_NAME = 'target_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE reports ADD COLUMN reason_type VARCHAR(64) NULL COMMENT ''骚扰/虚假资料/广告/诈骗/色情/其他''',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'reports' AND COLUMN_NAME = 'reason_type');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE reports ADD COLUMN reviewed_at DATETIME NULL',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'reports' AND COLUMN_NAME = 'reviewed_at');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE reports ADD COLUMN reviewed_by BIGINT NULL COMMENT ''审核管理员ID''',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'reports' AND COLUMN_NAME = 'reviewed_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
