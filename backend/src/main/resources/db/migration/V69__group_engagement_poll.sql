-- 字符串 ID 团体（platform_groups）投票：与 group_engagement_activity 同域
CREATE TABLE group_engagement_poll (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_external_id VARCHAR(128) NOT NULL,
    creator_user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    selection_mode VARCHAR(20) NOT NULL DEFAULT 'single',
    max_choices INT NOT NULL DEFAULT 1,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    results_public TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_engagement_poll_grp (group_external_id, status, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_engagement_poll_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    poll_id BIGINT NOT NULL,
    label VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_poll_option_poll (poll_id),
    CONSTRAINT fk_poll_option_poll FOREIGN KEY (poll_id) REFERENCES group_engagement_poll(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_engagement_poll_vote (
    id BIGINT NOT NULL AUTO_INCREMENT,
    poll_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_poll_user_option (poll_id, user_id, option_id),
    KEY idx_poll_vote_poll_user (poll_id, user_id),
    KEY idx_poll_vote_option (option_id),
    CONSTRAINT fk_poll_vote_poll FOREIGN KEY (poll_id) REFERENCES group_engagement_poll(id) ON DELETE CASCADE,
    CONSTRAINT fk_poll_vote_option FOREIGN KEY (option_id) REFERENCES group_engagement_poll_option(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
