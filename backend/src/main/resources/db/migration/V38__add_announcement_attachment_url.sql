SET @db_name = DATABASE();

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = @db_name
        AND table_name = 'announcements'
        AND column_name = 'attachment_url'
    ),
    'SELECT 1',
    'ALTER TABLE announcements ADD COLUMN attachment_url VARCHAR(512) DEFAULT NULL'
  )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
