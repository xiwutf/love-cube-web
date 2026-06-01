-- Phase 3.1 默契测试（价值观匹配）
CREATE TABLE match_compatibility_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(500) NOT NULL,
    option_a VARCHAR(200) NOT NULL,
    option_b VARCHAR(200) NOT NULL,
    option_c VARCHAR(200) NOT NULL,
    option_d VARCHAR(200) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_compatibility_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_pair_key VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option CHAR(1) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_compat_pair_user_q (match_pair_key, user_id, question_id),
    INDEX idx_compat_pair (match_pair_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO match_compatibility_question (question_text, option_a, option_b, option_c, option_d, sort_no, enabled) VALUES
('周末你更倾向怎么安排？', '宅家休息充电', '户外探索新地方', '和朋友聚会', '学习或培养爱好', 1, 1),
('对「异地恋」的看法是？', '完全不能接受', '短期可接受', '看感情基础', '距离不是问题', 2, 1),
('理想的生活节奏是？', '稳定规律', '忙碌充实', '自由随性', '平衡工作与生活', 3, 1),
('第一次约会你更看重？', '聊得来有共鸣', '对方的外在气质', '活动是否有趣', '是否真诚自然', 4, 1),
('关于未来规划，你更认同？', '先享受当下', '有清晰五年计划', '边走边看', '两人一起商量', 5, 1),
('遇到分歧时你通常？', '直接沟通说清', '先冷静再谈', '各退一步', '寻求第三方建议', 6, 1),
('你更欣赏哪种性格？', '幽默开朗', '沉稳可靠', '温柔细腻', '独立有主见', 7, 1),
('对社交媒体的看法？', '尽量少用', '日常分享生活', '仅作工具', '喜欢记录一切', 8, 1);

-- Phase 3.2 VIP 每日滑卡配额
CREATE TABLE user_daily_swipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    swipe_date DATE NOT NULL,
    swipe_count INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_swipe_date (user_id, swipe_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phase 3.3 活动签到码 + 互评
ALTER TABLE events ADD COLUMN checkin_code VARCHAR(16) DEFAULT NULL;

ALTER TABLE event_signups
    ADD COLUMN checked_in TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN checked_in_at DATETIME DEFAULT NULL;

CREATE TABLE event_peer_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    reviewer_user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(300) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_reviewer_target (event_id, reviewer_user_id, target_user_id),
    INDEX idx_event_target (event_id, target_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
