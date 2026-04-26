CREATE TABLE IF NOT EXISTS announcements (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(1000) NULL,
    content TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'draft',
    publish_date DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS articles (
    id VARCHAR(64) PRIMARY KEY,
    tag VARCHAR(64) NULL,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(1000) NULL,
    content TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'draft',
    publish_date DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS events (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(1000) NULL,
    content TEXT NULL,
    location VARCHAR(128) NULL,
    event_time DATETIME NULL,
    signup_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS verification_requests (
    id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    id_number VARCHAR(64) NOT NULL,
    note VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    reject_reason VARCHAR(500) NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reports (
    id VARCHAR(64) PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_user_id BIGINT NULL,
    report_type VARCHAR(64) NOT NULL,
    content VARCHAR(1000) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    note VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_feedback (
    id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(128) NULL,
    contact VARCHAR(128) NULL,
    content VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    admin_note VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feedback_user_id (user_id),
    INDEX idx_feedback_status (status),
    INDEX idx_feedback_created_at (created_at)
);

INSERT INTO announcements (id, title, summary, content, status, publish_date)
VALUES ('safety-upgrade-20260410', '平台安全策略升级公告', '新增异常登录提醒与账号保护说明，提升账号安全性。',
        '为进一步提升平台账户安全，我们已上线异常登录提醒与登录设备管理功能。', 'published', '2026-04-10 09:00:00')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO announcements (id, title, summary, content, status, publish_date)
VALUES ('review-rule-20260405', '联谊资料审核规范更新', '优化资料审核与匹配推荐规则，保障互动质量。',
        '本次更新主要针对资料完整度、头像真实性与个人描述规范。', 'published', '2026-04-05 10:00:00')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO announcements (id, title, summary, content, status, publish_date)
VALUES ('event-preview-20260328', '五一主题活动预告', '开放线上专题活动报名，支持站内消息通知。',
        '五一期间将上线系列主题活动，包含线上破冰、兴趣讨论与线下报名。', 'published', '2026-03-28 15:00:00')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO articles (id, tag, title, summary, content, status, publish_date)
VALUES ('profile-quality-guide', '平台资讯', '如何完善个人资料，提高匹配成功率',
        '从头像、签名、兴趣标签三个维度给出可执行建议。', '完善资料是提高匹配质量的第一步。', 'published', '2026-04-21 09:00:00')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO articles (id, tag, title, summary, content, status, publish_date)
VALUES ('platform-to-fellowship-path', '使用指南', '从平台官网到联谊模块的完整路径',
        '介绍两层架构下的访问方式与核心页面导航。', '平台官网用于内容展示和运营信息发布，联谊模块用于互动与聊天。', 'published', '2026-04-20 09:00:00')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO articles (id, tag, title, summary, content, status, publish_date)
VALUES ('communication-best-practice', '社区建议', '高质量沟通的五条实践建议',
        '帮助用户在聊天中更高效建立真实连接。', '高质量沟通建议包括尊重表达、适度自我介绍等。', 'published', '2026-04-19 09:00:00')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO events (id, title, summary, content, location, event_time, signup_count, status)
VALUES ('online-icebreak-night', '线上破冰夜', '语音连麦与主题互动环节，适合新用户快速加入。',
        '本活动面向新用户，设置轻量自我介绍、主题问答与自由连麦。', '线上直播间', '2026-05-01 19:30:00', 16, 'published')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO events (id, title, summary, content, location, event_time, signup_count, status)
VALUES ('interest-open-day', '兴趣小组开放日', '按兴趣分组讨论，支持后续私聊与活动追踪。',
        '活动将按阅读、旅行、电影、运动等主题分组。', '线上兴趣小组', '2026-05-11 20:00:00', 23, 'published')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO events (id, title, summary, content, location, event_time, signup_count, status)
VALUES ('offline-shanghai-meetup', '线下见面会（上海）', '平台认证用户可报名，现场设置安全签到与组织服务。',
        '线下活动仅向实名认证用户开放。', '上海线下会场', '2026-05-24 15:00:00', 30, 'published')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS fellowship_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nickname VARCHAR(64) NULL,
    avatar VARCHAR(255) NULL,
    gender VARCHAR(16) NULL,
    birthday DATE NULL,
    city VARCHAR(64) NULL,
    occupation VARCHAR(64) NULL,
    height INT NULL,
    bio VARCHAR(500) NULL,
    photos_json TEXT NULL,
    completion_rate INT NOT NULL DEFAULT 0,
    verification_status VARCHAR(32) NOT NULL DEFAULT 'none',
    verification_note VARCHAR(500) NULL,
    review_status VARCHAR(32) NOT NULL DEFAULT 'pending',
    review_note VARCHAR(500) NULL,
    reported_count INT NOT NULL DEFAULT 0,
    last_active_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fellowship_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nickname VARCHAR(64) NULL,
    gender VARCHAR(16) NULL,
    birth_year INT NULL,
    age INT NULL,
    city VARCHAR(64) NULL,
    occupation VARCHAR(64) NULL,
    education VARCHAR(64) NULL,
    height INT NULL,
    bio VARCHAR(500) NULL,
    intention VARCHAR(500) NULL,
    avatar_url VARCHAR(255) NULL,
    tags TEXT NULL,
    profile_status VARCHAR(16) NOT NULL DEFAULT 'INCOMPLETE',
    review_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

SET @db = DATABASE();

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN invite_code VARCHAR(32) NULL', 'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'invite_code'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN invited_by_user_id BIGINT NULL', 'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'invited_by_user_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN register_ip VARCHAR(64) NULL', 'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'register_ip'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN register_user_agent VARCHAR(500) NULL', 'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'register_user_agent'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN user_status VARCHAR(32) NOT NULL DEFAULT ''NORMAL''', 'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'user_status'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT ''USER''', 'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN invite_code_status VARCHAR(32) NOT NULL DEFAULT ''ENABLED''', 'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'invite_code_status'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE users
SET role = CASE
    WHEN UPPER(COALESCE(user_status, '')) = 'ROOT' THEN 'ROOT'
    WHEN UPPER(COALESCE(user_status, '')) = 'SUPER_ADMIN' THEN 'SUPER_ADMIN'
    WHEN UPPER(COALESCE(user_status, '')) = 'ADMIN' THEN 'ADMIN'
    ELSE role
END
WHERE UPPER(COALESCE(user_status, '')) IN ('ADMIN', 'SUPER_ADMIN', 'ROOT');

UPDATE users
SET role = 'ADMIN'
WHERE phone_number IN ('13800000000', '15030251407')
  AND UPPER(COALESCE(role, '')) NOT IN ('ADMIN', 'SUPER_ADMIN', 'ROOT');

UPDATE users
SET role = 'USER'
WHERE role IS NULL OR TRIM(role) = '';

SET @sql = (
    SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD UNIQUE INDEX uk_users_invite_code (invite_code)', 'SELECT 1')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND INDEX_NAME = 'uk_users_invite_code'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS invite_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invite_code VARCHAR(32) NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    invitee_user_id BIGINT NULL,
    invitee_username VARCHAR(128) NULL,
    register_ip VARCHAR(64) NULL,
    register_user_agent VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_invite_code (invite_code),
    INDEX idx_inviter_user_id (inviter_user_id),
    INDEX idx_invitee_user_id (invitee_user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
);
