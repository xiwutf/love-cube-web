-- Invite-only registration support for Love Cube
-- Compatible with MySQL 5.7 / 8.0 (without ADD COLUMN IF NOT EXISTS)

SET @db = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN invite_code VARCHAR(32) NULL COMMENT ''用户自己的邀请码''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'invite_code'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN invited_by_user_id BIGINT NULL COMMENT ''邀请人 user_id''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'invited_by_user_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN register_ip VARCHAR(64) NULL COMMENT ''注册IP''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'register_ip'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN register_user_agent VARCHAR(500) NULL COMMENT ''注册设备UserAgent''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'register_user_agent'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN user_status VARCHAR(32) NOT NULL DEFAULT ''NORMAL'' COMMENT ''NORMAL / DISABLED''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'user_status'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN invite_code_status VARCHAR(32) NOT NULL DEFAULT ''ENABLED'' COMMENT ''ENABLED / DISABLED''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'invite_code_status'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD UNIQUE INDEX uk_users_invite_code (invite_code)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND INDEX_NAME = 'uk_users_invite_code'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS invite_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invite_code VARCHAR(32) NOT NULL COMMENT '本次使用的邀请码',
    inviter_user_id BIGINT NOT NULL COMMENT '邀请人',
    invitee_user_id BIGINT NULL COMMENT '被邀请人',
    invitee_username VARCHAR(128) NULL COMMENT '被邀请人昵称快照',
    register_ip VARCHAR(64) NULL COMMENT '注册IP',
    register_user_agent VARCHAR(500) NULL COMMENT '注册设备信息',
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS / DISABLED / INVALID / FAILED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_invite_code (invite_code),
    INDEX idx_inviter_user_id (inviter_user_id),
    INDEX idx_invitee_user_id (invitee_user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
);
