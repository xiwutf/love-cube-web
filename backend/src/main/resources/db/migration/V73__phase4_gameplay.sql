-- Phase 4.1 本地服务扩展类型（招聘/二手/租房）种子
INSERT INTO local_resource (title, type, location, summary, heat, interest_count, status, created_at, updated_at) VALUES
('保定高新区 · 前端工程师', 'job', '河北保定', '3-5年经验，Vue/React 均可，远程可谈', 12, 3, 'published', NOW(), NOW()),
('莲池区 · 运营专员', 'job', '河北保定', '社群运营方向，有平台增长经验优先', 8, 2, 'published', NOW(), NOW()),
('九成新 iPad Air', 'second_hand', '河北保定', '自用闲置，含原装壳，当面验货', 15, 5, 'published', NOW(), NOW()),
('宜家书桌 + 人体工学椅', 'second_hand', '河北保定', '搬家转让，可小刀', 10, 4, 'published', NOW(), NOW()),
('竞秀区两居室整租', 'rental', '河北保定', '近地铁，押一付三，可养宠物', 20, 6, 'published', NOW(), NOW()),
('共享办公工位', 'rental', '河北保定', '按月起租，含网络与会议室', 6, 1, 'published', NOW(), NOW());

-- Phase 4.3 兴趣话题广场
CREATE TABLE interest_topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    post_count INT NOT NULL DEFAULT 0,
    heat INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE interest_topic_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_topic_post_topic (topic_id, created_at DESC),
    INDEX idx_topic_post_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO interest_topic (title, description, post_count, heat, sort_no, enabled) VALUES
('城市探索', '分享你所在城市的宝藏去处与周末路线', 0, 10, 1, 1),
('读书共读', '最近在读什么？欢迎推荐与短评', 0, 8, 2, 1),
('职场成长', '技能提升、转岗经验与副业探索', 0, 12, 3, 1),
('运动健身', '跑步、球类、居家训练打卡', 0, 6, 4, 1),
('美食探店', '好吃不踩雷的店铺清单', 0, 9, 5, 1),
('摄影创作', '作品交流与小技巧', 0, 5, 6, 1);

-- Phase 4.5 平台会员（与联谊 VIP 独立）
ALTER TABLE users ADD COLUMN platform_member_expires_at DATETIME DEFAULT NULL;
