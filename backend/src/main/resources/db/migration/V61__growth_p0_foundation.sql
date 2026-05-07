CREATE TABLE IF NOT EXISTS growth_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    actor_user_id BIGINT NOT NULL,
    target_user_id BIGINT NULL,
    biz_ref_type VARCHAR(64) NULL,
    biz_ref_id VARCHAR(128) NULL,
    rule_version VARCHAR(32) NOT NULL DEFAULT 'v1',
    source_platform VARCHAR(32) NOT NULL,
    settle_status VARCHAR(16) NOT NULL,
    dedupe_key VARCHAR(128) NOT NULL,
    payload_json JSON NULL,
    occurred_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_growth_event_dedupe_key (dedupe_key),
    KEY idx_growth_event_actor_time (actor_user_id, occurred_at),
    KEY idx_growth_event_type_time (event_type, occurred_at)
);

CREATE TABLE IF NOT EXISTS contribution_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    dimension VARCHAR(16) NOT NULL,
    delta_base INT NOT NULL DEFAULT 0,
    delta_final INT NOT NULL DEFAULT 0,
    weight_json JSON NULL,
    rule_version VARCHAR(32) NOT NULL DEFAULT 'v1',
    source_platform VARCHAR(32) NOT NULL,
    settle_status VARCHAR(16) NOT NULL,
    dedupe_key VARCHAR(128) NOT NULL,
    reason VARCHAR(255) NULL,
    occurred_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contribution_log_dedupe_key (dedupe_key),
    KEY idx_contribution_user_time (user_id, occurred_at),
    KEY idx_contribution_event_time (event_type, occurred_at),
    KEY idx_contribution_settle_status (settle_status)
);

CREATE TABLE IF NOT EXISTS user_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    achievement_code VARCHAR(64) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    event_id BIGINT NULL,
    rule_version VARCHAR(32) NOT NULL DEFAULT 'v1',
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_achievement_user (user_id),
    KEY idx_user_achievement_code (achievement_code)
);

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_growth'
      AND COLUMN_NAME = 'stage'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_growth ADD COLUMN stage VARCHAR(32) NOT NULL DEFAULT ''normal''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_growth'
      AND COLUMN_NAME = 'total_contribution'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_growth ADD COLUMN total_contribution INT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_growth'
      AND COLUMN_NAME = 'contrib_content'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_growth ADD COLUMN contrib_content INT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_growth'
      AND COLUMN_NAME = 'contrib_org'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_growth ADD COLUMN contrib_org INT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_growth'
      AND COLUMN_NAME = 'contrib_spread'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_growth ADD COLUMN contrib_spread INT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_growth'
      AND COLUMN_NAME = 'contrib_city'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_growth ADD COLUMN contrib_city INT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_growth'
      AND COLUMN_NAME = 'rule_version'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_growth ADD COLUMN rule_version VARCHAR(32) NOT NULL DEFAULT ''v1''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
