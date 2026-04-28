SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'fellowship_enabled'
);

SET @ddl = IF(
    @col_exists = 0,
    "ALTER TABLE users ADD COLUMN fellowship_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已手动开通联谊模块'",
    "SELECT 1"
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
