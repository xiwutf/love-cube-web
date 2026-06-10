-- 团体迭代 2/3：活动签到互评、平台活动关联、周报去重

ALTER TABLE platform_group_activity
    ADD COLUMN checkin_code VARCHAR(16) DEFAULT NULL,
    ADD COLUMN platform_event_id VARCHAR(64) DEFAULT NULL;

ALTER TABLE platform_group_activity_signup
    ADD COLUMN checked_in TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN checked_in_at DATETIME DEFAULT NULL;

CREATE TABLE group_activity_peer_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    reviewer_user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(300) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_reviewer_target (activity_id, reviewer_user_id, target_user_id),
    KEY idx_activity_target (activity_id, target_user_id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE platform_group_weekly_digest_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    week_key VARCHAR(16) NOT NULL,
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_week (group_id, week_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
