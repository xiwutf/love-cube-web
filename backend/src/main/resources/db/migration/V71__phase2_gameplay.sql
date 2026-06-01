-- Phase 2.1 每周挑战任务
CREATE TABLE weekly_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_count INT NOT NULL DEFAULT 1,
    reward_exp INT NOT NULL DEFAULT 10,
    sort_no INT DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_weekly_task_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_code VARCHAR(50) NOT NULL,
    week_start DATE NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    completed TINYINT NOT NULL DEFAULT 0,
    claimed TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_weekly_task (user_id, task_code, week_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO weekly_tasks (code, name, action_type, target_count, reward_exp, sort_no, enabled) VALUES
('WEEKLY_LOGIN_5', '本周登录 5 天', 'LOGIN', 5, 30, 1, 1),
('WEEKLY_POST_3', '本周发布 3 条内容', 'POST_CONTENT', 3, 25, 2, 1),
('WEEKLY_LIKE_10', '本周点赞 10 次', 'LIKE_CONTENT', 10, 20, 3, 1),
('WEEKLY_COMMENT_3', '本周评论 3 次', 'COMMENT_CONTENT', 3, 20, 4, 1);

-- Phase 2.2 新人 7 日任务包
CREATE TABLE newcomer_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    unlock_day INT NOT NULL,
    check_type VARCHAR(40) NOT NULL,
    check_value INT NOT NULL DEFAULT 1,
    reward_exp INT NOT NULL DEFAULT 20,
    sort_no INT DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_newcomer_task_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_code VARCHAR(50) NOT NULL,
    completed TINYINT NOT NULL DEFAULT 0,
    claimed TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_newcomer_task (user_id, task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO newcomer_tasks (code, name, unlock_day, check_type, check_value, reward_exp, sort_no) VALUES
('NEWCOMER_D1_LOGIN', '第 1 天：完成首次登录', 1, 'LOGIN_COUNT', 1, 15, 1),
('NEWCOMER_D2_POST', '第 2 天：发布 1 条心声', 2, 'POST_COUNT', 1, 20, 2),
('NEWCOMER_D3_GROUP', '第 3 天：加入一个团体', 3, 'IN_APPROVED_PLATFORM_GROUP', 1, 25, 3),
('NEWCOMER_D4_LIKE', '第 4 天：点赞 3 次内容', 4, 'LIKE_COUNT', 3, 20, 4),
('NEWCOMER_D5_COMMENT', '第 5 天：评论 1 次', 5, 'COMMENT_COUNT', 1, 20, 5),
('NEWCOMER_D6_VIEW', '第 6 天：浏览 5 条内容', 6, 'VIEW_COUNT', 5, 15, 6),
('NEWCOMER_D7_STREAK', '第 7 天：累计登录 3 天', 7, 'LOGIN_COUNT', 3, 30, 7);

-- Phase 2.3 每日心声话题
CREATE TABLE positive_share_daily_topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_date DATE NOT NULL,
    topic_text VARCHAR(300) NOT NULL,
    hint_text VARCHAR(500) DEFAULT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_topic_date (topic_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO positive_share_daily_topic (topic_date, topic_text, hint_text) VALUES
(CURDATE(), '今天，你最想感谢的一件事是什么？', '可以是某个人、某个瞬间，或一件小事'),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '分享一个最近让你微笑的瞬间', '照片、文字、或一句描述都可以'),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '如果给本周的自己打一句话评语，你会写什么？', '鼓励自己，也欢迎真实表达'),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '你最近在读/在看/在学什么？', '书籍、课程、播客、技能都算'),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '说一件你正在坚持的小习惯', '运动、阅读、早睡、记录…'),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '给陌生人一句温暖的祝福', '越具体越动人'),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '本周你最大的收获是什么？', '大小不限，真实就好');

-- Phase 2.4 互助信用分
ALTER TABLE user_help_stats ADD COLUMN credit_score INT NOT NULL DEFAULT 0;

UPDATE user_help_stats
SET credit_score = help_count * 1 + accepted_count * 3 + success_count * 10;
