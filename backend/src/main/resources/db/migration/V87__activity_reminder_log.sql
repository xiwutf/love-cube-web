CREATE TABLE activity_reminder_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity_source VARCHAR(32) NOT NULL COMMENT 'EVENT / GROUP_ACTIVITY',
    activity_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    reminder_type VARCHAR(32) NOT NULL COMMENT 'REMINDER_24H / REMINDER_2H / REVIEW_REMINDER',
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_reminder (activity_source, activity_id, user_id, reminder_type),
    KEY idx_reminder_sent (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
