-- Add verify_type, submit_data, reviewer_id to user_verifications (idempotent)
SET @db = DATABASE();

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE user_verifications ADD COLUMN verify_type VARCHAR(32) NULL COMMENT ''REALNAME/PHOTO/IDCARD''',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_verifications' AND COLUMN_NAME = 'verify_type');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE user_verifications ADD COLUMN submit_data TEXT NULL COMMENT ''JSON submission payload''',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_verifications' AND COLUMN_NAME = 'submit_data');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE user_verifications ADD COLUMN reviewer_id BIGINT NULL COMMENT ''admin user id who reviewed''',
  'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_verifications' AND COLUMN_NAME = 'reviewer_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Back-fill existing rows that have id_number as REALNAME type
UPDATE user_verifications
SET verify_type = 'REALNAME'
WHERE verify_type IS NULL AND id_number IS NOT NULL AND id_number <> '';

UPDATE user_verifications
SET verify_type = 'PHOTO'
WHERE verify_type IS NULL AND selfie_url IS NOT NULL AND selfie_url <> '';
