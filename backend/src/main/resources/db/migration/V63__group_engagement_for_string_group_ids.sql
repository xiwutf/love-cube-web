-- 字符串 ID 团体（platform_groups / group_members）的打卡、日任务与活动，
-- 与数字 ID 团体（platform_group / platform_group_member）的 plat 表互不混用。
-- （由原 V61 升格为 V63：与 V61 growth_p0_foundation、V62 group_posts_like_comment 去重编号）
CREATE TABLE group_engagement_checkin (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_external_id VARCHAR(128) NOT NULL,
    user_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    checkin_type VARCHAR(32) NOT NULL DEFAULT 'other',
    content VARCHAR(500),
    streak_days INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_engagement_grp_user_date (group_external_id, user_id, checkin_date),
    KEY idx_engagement_grp_date (group_external_id, checkin_date),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_engagement_task_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_external_id VARCHAR(128) NOT NULL,
    user_id BIGINT NOT NULL,
    task_code VARCHAR(32) NOT NULL,
    task_date DATE NOT NULL,
    completed TINYINT(1) NOT NULL DEFAULT 0,
    claimed TINYINT(1) NOT NULL DEFAULT 0,
    completed_at DATETIME NULL,
    claimed_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_engagement_task_day (group_external_id, user_id, task_code, task_date),
    KEY idx_grp_user_day (group_external_id, user_id, task_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_engagement_activity (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_external_id VARCHAR(128) NOT NULL,
    creator_user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    location VARCHAR(200),
    max_participants INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    participant_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_grp_status_start (group_external_id, status, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_engagement_activity_signup (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    group_external_id VARCHAR(128) NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'signed_up',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
