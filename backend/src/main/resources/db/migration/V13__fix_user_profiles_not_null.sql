-- Fix NOT NULL constraints on user_profiles that V7 failed to remove (inverted idempotency logic bug)
SET @db = DATABASE();

SET @sql = (SELECT IF(COUNT(*) > 0,
  'ALTER TABLE user_profiles MODIFY COLUMN avatar VARCHAR(255) NULL',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles'
    AND COLUMN_NAME = 'avatar' AND IS_NULLABLE = 'NO');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) > 0,
  'ALTER TABLE user_profiles MODIFY COLUMN nickname VARCHAR(50) NULL',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles'
    AND COLUMN_NAME = 'nickname' AND IS_NULLABLE = 'NO');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) > 0,
  'ALTER TABLE user_profiles MODIFY COLUMN age INT NULL',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles'
    AND COLUMN_NAME = 'age' AND IS_NULLABLE = 'NO');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) > 0,
  'ALTER TABLE user_profiles MODIFY COLUMN gender VARCHAR(10) NULL',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles'
    AND COLUMN_NAME = 'gender' AND IS_NULLABLE = 'NO');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
