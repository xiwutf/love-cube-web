-- 邀请关系：记录有效邀请达成时间
ALTER TABLE user_invite_relation ADD COLUMN effective_at DATETIME DEFAULT NULL;

-- 用户连续登录 streak
CREATE TABLE user_login_streak (
    user_id BIGINT NOT NULL PRIMARY KEY,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_login_date DATE DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 团体赛季
CREATE TABLE platform_group_season (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    season_code VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform_group_season_code (season_code),
    INDEX idx_platform_group_season_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE platform_group_season_score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    season_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    score INT NOT NULL DEFAULT 0,
    checkin_count INT NOT NULL DEFAULT 0,
    task_count INT NOT NULL DEFAULT 0,
    activity_count INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_season_group (season_id, group_id),
    INDEX idx_season_score (season_id, score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 破冰话题
CREATE TABLE match_icebreaker_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(500) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_icebreaker_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_pair_key VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pair_user_question (match_pair_key, user_id, question_id),
    INDEX idx_pair_key (match_pair_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO match_icebreaker_question (question_text, sort_no, enabled) VALUES
('你最近最开心的一件事是什么？', 1, 1),
('周末你更喜欢宅家还是出门探索？', 2, 1),
('如果只能推荐一本书/一部电影，你会选什么？', 3, 1),
('你理想中一次完美的约会是什么样的？', 4, 1),
('你最近在学习或尝试什么新东西？', 5, 1),
('你觉得自己最被朋友认可的一个优点是什么？', 6, 1);
