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

INSERT INTO announcements (id, title, summary, content, status, publish_date)
SELECT 'safety-upgrade-20260410', '平台安全策略升级公告', '新增异常登录提醒与账号保护说明，提升账号安全性。',
       '为进一步提升平台账户安全，我们已上线异常登录提醒与登录设备管理功能。', 'published', '2026-04-10 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'safety-upgrade-20260410');

INSERT INTO announcements (id, title, summary, content, status, publish_date)
SELECT 'review-rule-20260405', '联谊资料审核规范更新', '优化资料审核与匹配推荐规则，保障互动质量。',
       '本次更新主要针对资料完整度、头像真实性与个人描述规范。', 'published', '2026-04-05 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'review-rule-20260405');

INSERT INTO announcements (id, title, summary, content, status, publish_date)
SELECT 'event-preview-20260328', '五一主题活动预告', '开放线上专题活动报名，支持站内消息通知。',
       '五一期间将上线系列主题活动，包含线上破冰、兴趣讨论与线下报名。', 'published', '2026-03-28 15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'event-preview-20260328');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date)
SELECT 'profile-quality-guide', '平台资讯', '如何完善个人资料，提高匹配成功率',
       '从头像、签名、兴趣标签三个维度给出可执行建议。', '完善资料是提高匹配质量的第一步。', 'published', '2026-04-21 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'profile-quality-guide');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date)
SELECT 'platform-to-fellowship-path', '使用指南', '从平台官网到联谊模块的完整路径',
       '介绍两层架构下的访问方式与核心页面导航。', '平台官网用于内容展示和运营信息发布，联谊模块用于互动与聊天。', 'published', '2026-04-20 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'platform-to-fellowship-path');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date)
SELECT 'communication-best-practice', '社区建议', '高质量沟通的五条实践建议',
       '帮助用户在聊天中更高效建立真实连接。', '高质量沟通建议包括尊重表达、适度自我介绍等。', 'published', '2026-04-19 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'communication-best-practice');

INSERT INTO events (id, title, summary, content, location, event_time, signup_count, status)
SELECT 'online-icebreak-night', '线上破冰夜', '语音连麦与主题互动环节，适合新用户快速加入。',
       '本活动面向新用户，设置轻量自我介绍、主题问答与自由连麦。', '线上直播间', '2026-05-01 19:30:00', 16, 'published'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE id = 'online-icebreak-night');

INSERT INTO events (id, title, summary, content, location, event_time, signup_count, status)
SELECT 'interest-open-day', '兴趣小组开放日', '按兴趣分组讨论，支持后续私聊与活动追踪。',
       '活动将按阅读、旅行、电影、运动等主题分组。', '线上兴趣小组', '2026-05-11 20:00:00', 23, 'published'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE id = 'interest-open-day');

INSERT INTO events (id, title, summary, content, location, event_time, signup_count, status)
SELECT 'offline-shanghai-meetup', '线下见面会（上海）', '平台认证用户可报名，现场设置安全签到与组织服务。',
       '线下活动仅向实名认证用户开放。', '上海线下会场', '2026-05-24 15:00:00', 30, 'published'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE id = 'offline-shanghai-meetup');
