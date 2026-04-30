SET @db_name = DATABASE();

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = @db_name
        AND table_name = 'announcements'
        AND column_name = 'popup_enabled'
    ),
    'SELECT 1',
    'ALTER TABLE announcements ADD COLUMN popup_enabled TINYINT(1) NOT NULL DEFAULT 0'
  )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
