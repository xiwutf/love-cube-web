CREATE TABLE user_growth (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    level INT NOT NULL DEFAULT 1,
    exp INT NOT NULL DEFAULT 0,
    title VARCHAR(50) DEFAULT '新手用户',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_growth_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    biz_id VARCHAR(100) NOT NULL,
    exp INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_action_biz (user_id, action_type, biz_id)
);

CREATE TABLE badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(255),
    badge_type VARCHAR(30),
    condition_value INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    badge_code VARCHAR(50) NOT NULL,
    progress INT DEFAULT 0,
    unlocked TINYINT DEFAULT 0,
    unlocked_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_badge (user_id, badge_code)
);

CREATE TABLE daily_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_count INT NOT NULL DEFAULT 1,
    reward_exp INT NOT NULL DEFAULT 1,
    sort_no INT DEFAULT 0,
    enabled TINYINT DEFAULT 1
);

CREATE TABLE user_daily_task_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_code VARCHAR(50) NOT NULL,
    task_date DATE NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    completed TINYINT DEFAULT 0,
    claimed TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_task_date (user_id, task_code, task_date)
);

INSERT INTO badges (code, name, description, icon, badge_type, condition_value) VALUES
('FIRST_LOGIN', '初来乍到', '首次登录平台', 'badge-first-login', 'newbie', 1),
('FIRST_POST', '内容发布者', '发布第一条内容', 'badge-first-post', 'content', 1),
('DAILY_CHECK_3', '每日打卡', '累计完成3次每日任务', 'badge-daily-check', 'active', 3),
('JOIN_GROUP', '团体成员', '加入或创建一个团体', 'badge-join-group', 'group', 1);

INSERT INTO daily_tasks (code, name, action_type, target_count, reward_exp, sort_no) VALUES
('DAILY_LOGIN', '每日登录', 'LOGIN', 1, 2, 1),
('DAILY_POST', '发布1条内容', 'POST_CONTENT', 1, 10, 2),
('DAILY_VIEW', '浏览3条内容', 'VIEW_CONTENT', 3, 3, 3),
('DAILY_LIKE', '点赞1条内容', 'LIKE_CONTENT', 1, 2, 4);
