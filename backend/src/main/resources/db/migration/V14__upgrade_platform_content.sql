-- Add category, cover_url, pinned, recommended, view_count to all three content tables

SET @db_name = DATABASE();

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'announcements' AND column_name = 'category'
    ),
    'SELECT 1',
    'ALTER TABLE announcements ADD COLUMN category VARCHAR(64) DEFAULT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'announcements' AND column_name = 'cover_url'
    ),
    'SELECT 1',
    'ALTER TABLE announcements ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'announcements' AND column_name = 'pinned'
    ),
    'SELECT 1',
    'ALTER TABLE announcements ADD COLUMN pinned TINYINT(1) DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'announcements' AND column_name = 'recommended'
    ),
    'SELECT 1',
    'ALTER TABLE announcements ADD COLUMN recommended TINYINT(1) DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'announcements' AND column_name = 'view_count'
    ),
    'SELECT 1',
    'ALTER TABLE announcements ADD COLUMN view_count INT DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'articles' AND column_name = 'category'
    ),
    'SELECT 1',
    'ALTER TABLE articles ADD COLUMN category VARCHAR(64) DEFAULT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'articles' AND column_name = 'cover_url'
    ),
    'SELECT 1',
    'ALTER TABLE articles ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'articles' AND column_name = 'pinned'
    ),
    'SELECT 1',
    'ALTER TABLE articles ADD COLUMN pinned TINYINT(1) DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'articles' AND column_name = 'recommended'
    ),
    'SELECT 1',
    'ALTER TABLE articles ADD COLUMN recommended TINYINT(1) DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'articles' AND column_name = 'view_count'
    ),
    'SELECT 1',
    'ALTER TABLE articles ADD COLUMN view_count INT DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'events' AND column_name = 'category'
    ),
    'SELECT 1',
    'ALTER TABLE events ADD COLUMN category VARCHAR(64) DEFAULT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'events' AND column_name = 'cover_url'
    ),
    'SELECT 1',
    'ALTER TABLE events ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'events' AND column_name = 'pinned'
    ),
    'SELECT 1',
    'ALTER TABLE events ADD COLUMN pinned TINYINT(1) DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'events' AND column_name = 'recommended'
    ),
    'SELECT 1',
    'ALTER TABLE events ADD COLUMN recommended TINYINT(1) DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = @db_name AND table_name = 'events' AND column_name = 'view_count'
    ),
    'SELECT 1',
    'ALTER TABLE events ADD COLUMN view_count INT DEFAULT 0'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
