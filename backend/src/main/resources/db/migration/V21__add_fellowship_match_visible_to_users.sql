SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'fellowship_match_visible'
);

SET @ddl = IF(
    @col_exists = 0,
    "ALTER TABLE users ADD COLUMN fellowship_match_visible TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否进入联谊匹配列表'",
    "SELECT 1"
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
