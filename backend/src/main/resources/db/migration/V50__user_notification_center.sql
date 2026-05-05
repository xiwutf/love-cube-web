-- 站内消息中心、通知设置、第三方绑定（微信等预留）
-- 兼容：① 从未创建过 notifications 的空库路径；② 仍存在旧表 notifications 的升级路径（迁移后删除旧表）。
-- 迁移使用动态 SQL，避免在旧表不存在时静态解析 FROM notifications 导致整段迁移失败。

CREATE TABLE IF NOT EXISTS user_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收用户',
    type VARCHAR(64) NOT NULL COMMENT '业务通知类型',
    level VARCHAR(32) NOT NULL COMMENT 'IMPORTANT/INTERACTION/ANNOUNCEMENT/SYSTEM',
    title VARCHAR(256) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    link_url VARCHAR(512) NULL,
    related_id VARCHAR(128) NULL,
    related_type VARCHAR(64) NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    push_channel VARCHAR(32) NOT NULL DEFAULT 'SITE' COMMENT '预留：SITE 等',
    push_status VARCHAR(32) NOT NULL DEFAULT 'SKIPPED' COMMENT 'PENDING/SKIPPED/MOCK_SENT/FAILED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    read_at DATETIME(6) NULL,
    INDEX idx_un_user_created (user_id, created_at),
    INDEX idx_un_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_notification_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(64) NOT NULL,
    site_enabled TINYINT(1) NOT NULL DEFAULT 1,
    wechat_enabled TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_uns_user_type (user_id, type),
    INDEX idx_uns_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_social_bindings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(64) NOT NULL COMMENT 'WECHAT_OFFICIAL/WECHAT_MINI_PROGRAM/QQ',
    openid VARCHAR(128) NOT NULL,
    unionid VARCHAR(128) NULL,
    nickname VARCHAR(128) NULL,
    avatar_url VARCHAR(512) NULL,
    bind_status VARCHAR(32) NOT NULL DEFAULT 'BOUND',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_usb_user_provider (user_id, provider),
    INDEX idx_usb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @db := DATABASE();
SET @has_old := (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'notifications'
);
-- 字段映射：type 归一化；level 按旧 type 推断；related_* <- target_*；is_read 容错；其余原样保留
SET @sql := IF(@has_old > 0,
    'INSERT INTO user_notifications (user_id, type, level, title, content, link_url, related_id, related_type, is_read, push_channel, push_status, created_at, read_at)
     SELECT n.user_id,
            CASE n.type
                WHEN ''GROUP_JOIN_APPROVED'' THEN ''GROUP_APPLICATION_APPROVED''
                WHEN ''GROUP_JOIN_REJECTED'' THEN ''GROUP_APPLICATION_REJECTED''
                ELSE n.type END,
            CASE
                WHEN n.type IN (''LIKE'') THEN ''INTERACTION''
                WHEN n.type IN (''GROUP_POST_LIKED'',''GROUP_POST_COMMENTED'',''GROUP_POST_CREATED'') THEN ''INTERACTION''
                WHEN n.type IN (''GROUP_JOIN_APPROVED'',''GROUP_JOIN_REJECTED'') THEN ''IMPORTANT''
                WHEN n.type IN (''MESSAGE'') THEN ''SYSTEM''
                ELSE ''SYSTEM'' END,
            n.title,
            n.content,
            NULL,
            n.target_id,
            n.target_type,
            COALESCE(n.is_read, 0),
            ''SITE'',
            ''SKIPPED'',
            COALESCE(n.created_at, CURRENT_TIMESTAMP(6)),
            n.read_at
     FROM notifications n',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql2 := IF(@has_old > 0, 'DROP TABLE IF EXISTS notifications', 'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;
