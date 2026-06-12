-- 用户级通知渠道偏好（邮件 / PushPlus）与投递记录

CREATE TABLE user_notification_channel_prefs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    email_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否开启邮件通知',
    pushplus_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否开启 PushPlus 微信推送',
    pushplus_token VARCHAR(128) NULL COMMENT 'PushPlus token',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_uncp_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_dispatch_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL COMMENT '关联站内消息',
    user_id BIGINT NOT NULL COMMENT '接收用户',
    channel VARCHAR(32) NOT NULL COMMENT 'EMAIL / PUSHPLUS',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / SENT / FAILED / SKIPPED',
    error_message VARCHAR(512) NULL,
    sent_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_ndr_notification (notification_id),
    INDEX idx_ndr_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
