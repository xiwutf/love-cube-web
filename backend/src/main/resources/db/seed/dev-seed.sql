-- Love Cube development seed data
-- Scope: dev / staging only, never production
-- Idempotent design: insert-if-not-exists based on stable seed keys

SET NAMES utf8mb4;

START TRANSACTION;

-- 1) Platform announcements (5)
INSERT INTO announcements (
    id, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at
)
SELECT
    'dev-seed-announcement-01',
    '保定线下交流会报名须知',
    '发布报名时间、签到要求与现场安全指引，帮助新用户快速了解活动流程。',
    '本周末将举办保定小型线下交流会，已完成资料完善与实名认证的用户可优先报名。活动现场设置签到核验、分组破冰和自由交流三个环节，请提前十分钟到场。',
    'published',
    NOW() - INTERVAL 2 DAY,
    '活动公告',
    'https://images.unsplash.com/photo-1515169067868-5387ec356754',
    1, 1, 286, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'dev-seed-announcement-01');

INSERT INTO announcements (
    id, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at
)
SELECT
    'dev-seed-announcement-02',
    '石家庄新用户资料完善周开启',
    '平台将提供资料优化建议与审核加急通道，提升匹配推荐效率。',
    '石家庄城市站本周开启资料完善周，系统会根据头像、个人介绍、择偶意向给出修改建议。完成度达到 90 分以上的用户会进入优先推荐队列。',
    'published',
    NOW() - INTERVAL 1 DAY,
    '平台动态',
    'https://images.unsplash.com/photo-1497366754035-f200968a6e72',
    0, 1, 198, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'dev-seed-announcement-02');

INSERT INTO announcements (
    id, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at
)
SELECT
    'dev-seed-announcement-03',
    '北京城市专场活动排期更新',
    '新增周三晚间沙龙场次，方便工作日参与。',
    '北京城市专场从本月起新增每周三晚间轻社交沙龙，主打小规模深度交流。活动以兴趣主题讨论为主，报名后请留意站内消息中的分组通知。',
    'published',
    NOW() - INTERVAL 4 HOUR,
    '活动公告',
    'https://images.unsplash.com/photo-1521737604893-d14cc237f11d',
    0, 0, 143, NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'dev-seed-announcement-03');

INSERT INTO announcements (
    id, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at
)
SELECT
    'dev-seed-announcement-04',
    '头像与相册内容规范更新说明',
    '进一步明确头像、生活照、场景照审核规则，减少误判与重复提交。',
    '头像建议使用半年内清晰正脸照，相册建议至少上传三张包含日常场景的照片。平台将优先审核符合规范的资料，审核结果会通过站内通知同步。',
    'published',
    NOW() - INTERVAL 5 DAY,
    '规则更新',
    'https://images.unsplash.com/photo-1529156069898-49953e39b3ac',
    0, 0, 275, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'dev-seed-announcement-04');

INSERT INTO announcements (
    id, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at
)
SELECT
    'dev-seed-announcement-05',
    '节前线上主题夜谈活动预告',
    '围绕“稳定关系中的沟通边界”展开主题交流，支持会后私聊延展。',
    '本次线上夜谈设置主持引导与匿名提问环节，参与用户可在活动后查看推荐联系人清单。活动名额有限，系统将按报名顺序与资料完整度综合排序。',
    'published',
    NOW() + INTERVAL 2 DAY,
    '活动预告',
    'https://images.unsplash.com/photo-1492684223066-81342ee5ff30',
    0, 1, 89, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR
WHERE NOT EXISTS (SELECT 1 FROM announcements WHERE id = 'dev-seed-announcement-05');

-- 2) Articles (10)
INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-01', '关系成长', '初次见面如何建立舒服的聊天节奏', '从开场问题到共鸣延展，给出可直接上手的表达方法。',
       '建议先从对方最近的生活状态切入，再延伸到兴趣和价值观。避免连续追问式交流，留出回应空间，更容易形成自然互动。', 'published', NOW() - INTERVAL 8 DAY,
       '交友指南', 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac', 0, 1, 356, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-01');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-02', '城市活动', '保定周末联谊活动路线推荐', '覆盖咖啡、书店与轻运动三类场景，适合首次线下见面。',
       '保定主城区可选择下午咖啡店破冰，傍晚公园散步，最后在安静餐厅继续交流。节奏由浅入深，能有效降低首次见面的紧张感。', 'published', NOW() - INTERVAL 6 DAY,
       '城市指南', 'https://images.unsplash.com/photo-1505238680356-667803448bb6', 0, 0, 241, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-02');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-03', '资料优化', '三步优化个人简介，提高匹配质量', '把“我是谁、我期待什么、我能提供什么”写清楚。',
       '简介建议控制在 80-150 字，避免空泛口号。加入真实生活片段和关系期待，系统会更准确地匹配价值观相近的人。', 'published', NOW() - INTERVAL 5 DAY,
       '平台使用', 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40', 1, 1, 418, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-03');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-04', '沟通方法', '关系推进中如何表达边界感', '边界不是拒绝，而是让关系更长期稳定。',
       '边界表达建议使用“我感受 + 我需要 + 可行方案”的结构。既能保护自己，也能让对方明确你的期待。', 'published', NOW() - INTERVAL 4 DAY,
       '关系建议', 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f', 0, 1, 287, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-04');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-05', '城市观察', '石家庄用户高频约会场景盘点', '从通勤便利和环境舒适度出发，筛选高成功率场景。',
       '石家庄用户更偏好交通便利、噪音较低的场景。推荐优先选择地铁沿线商圈内的咖啡馆与展览空间。', 'published', NOW() - INTERVAL 3 DAY,
       '城市指南', 'https://images.unsplash.com/photo-1497215842964-222b430dc094', 0, 0, 194, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-05');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-06', '线下活动', '北京小型读书沙龙参与指南', '提前准备话题卡与个人观点，现场交流更自然。',
       '建议围绕一本近期阅读书籍准备两个观点和一个问题。既能展示思考深度，也能快速找到兴趣相近的人。', 'published', NOW() - INTERVAL 2 DAY,
       '活动攻略', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f', 0, 1, 309, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-06');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-07', '关系经营', '从线上聊天到线下见面的转化技巧', '把握节奏与信任感，是转化成功的关键。',
       '当双方连续三天保持稳定互动后，可以尝试提出轻量见面邀约。建议给出具体时间段和备选地点，降低决策成本。', 'published', NOW() - INTERVAL 1 DAY,
       '交友指南', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4', 0, 1, 332, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-07');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-08', '平台功能', '首页 Banner 推荐位展示规则说明', '介绍推荐逻辑、排序策略与审核时间窗口。',
       '推荐位会综合内容质量、时效性与互动反馈进行排序。通过审核后通常在 10 分钟内完成缓存更新并展示。', 'published', NOW() - INTERVAL 10 HOUR,
       '产品更新', 'https://images.unsplash.com/photo-1460925895917-afdab827c52f', 0, 0, 121, NOW() - INTERVAL 10 HOUR, NOW() - INTERVAL 10 HOUR
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-08');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-09', '真实案例', '一次成功匹配背后的资料细节', '通过真实案例拆解高质量资料的共性。',
       '高质量资料通常具备真实头像、可验证生活场景和明确关系期待三要素。推荐列表中的停留时长也会明显提升。', 'published', NOW() - INTERVAL 6 HOUR,
       '案例复盘', 'https://images.unsplash.com/photo-1552664730-d307ca884978', 0, 1, 167, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-09');

INSERT INTO articles (id, tag, title, summary, content, status, publish_date, category, cover_url, pinned, recommended, view_count, created_at, updated_at)
SELECT 'dev-seed-article-10', '关系沟通', '长期关系中“稳定回应”为什么重要', '稳定回应能显著降低误解与情绪消耗。',
       '稳定回应不是秒回，而是持续、明确、可预期。约定沟通窗口并及时同步状态，关系中的安全感会显著提升。', 'published', NOW() - INTERVAL 2 HOUR,
       '关系建议', 'https://images.unsplash.com/photo-1485217988980-11786ced9454', 0, 0, 93, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR
WHERE NOT EXISTS (SELECT 1 FROM articles WHERE id = 'dev-seed-article-10');

-- 3) Platform events — intentionally omitted (create real activities via admin; Flyway V56 removes legacy demo rows).

-- 4) Home banners linking to seed events — omitted (same as above).

-- 5) Fellowship users (15)
INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '陈子衡', 'chenziheng@seed.lovecube.local', '13999010001', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_001',
       'https://images.unsplash.com/photo-1500648767791-00dcc994a43e', '喜欢慢跑和历史纪录片，周末常在保定周边短途徒步。', 29, 1, '保定', '建筑设计师', 178,
       '["https://images.unsplash.com/photo-1500648767791-00dcc994a43e","https://images.unsplash.com/photo-1494790108377-be9c29b29330"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC001', 'ENABLED', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_001');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '马慧琳', 'mahuilin@seed.lovecube.local', '13999010002', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_002',
       'https://images.unsplash.com/photo-1494790108377-be9c29b29330', '工作日忙于运营策划，空闲时喜欢逛展和做手账。', 27, 2, '石家庄', '新媒体运营', 165,
       '["https://images.unsplash.com/photo-1494790108377-be9c29b29330","https://images.unsplash.com/photo-1487412720507-e7ab37603c6f"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC002', 'ENABLED', NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_002');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '李沐阳', 'limuyang@seed.lovecube.local', '13999010003', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_003',
       'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e', '在北京从事产品工作，偏好有深度的对话和稳定关系。', 31, 1, '北京', '产品经理', 180,
       '["https://images.unsplash.com/photo-1472099645785-5658abf4ff4e","https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC003', 'ENABLED', NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_003');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '赵语晨', 'zhaoyuchen@seed.lovecube.local', '13999010004', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_004',
       'https://images.unsplash.com/photo-1487412720507-e7ab37603c6f', '语文老师，热爱阅读和城市散步，性格温和。', 26, 2, '保定', '中学语文老师', 163,
       '["https://images.unsplash.com/photo-1487412720507-e7ab37603c6f","https://images.unsplash.com/photo-1517841905240-472988babdf9"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC004', 'ENABLED', NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_004');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '孙嘉辰', 'sunjiachen@seed.lovecube.local', '13999010005', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_005',
       'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d', '软件工程师，偏向理性沟通，也愿意认真经营关系。', 28, 1, '石家庄', '软件工程师', 176,
       '["https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d","https://images.unsplash.com/photo-1504593811423-6dd665756598"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC005', 'ENABLED', NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_005');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '何思雨', 'hesiyu@seed.lovecube.local', '13999010006', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_006',
       'https://images.unsplash.com/photo-1517841905240-472988babdf9', '室内设计师，喜欢拍照和看展，希望结识真诚稳定的人。', 30, 2, '北京', '室内设计师', 168,
       '["https://images.unsplash.com/photo-1517841905240-472988babdf9","https://images.unsplash.com/photo-1524504388940-b1c1722653e1"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC006', 'ENABLED', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_006');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '郭安然', 'guoanran@seed.lovecube.local', '13999010007', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_007',
       'https://images.unsplash.com/photo-1524504388940-b1c1722653e1', '护理工作者，生活规律，期待认真相处的长期关系。', 25, 2, '保定', '护士', 162,
       '["https://images.unsplash.com/photo-1524504388940-b1c1722653e1","https://images.unsplash.com/photo-1534528741775-53994a69daeb"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC007', 'ENABLED', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_007');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '宋景川', 'songjingchuan@seed.lovecube.local', '13999010008', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_008',
       'https://images.unsplash.com/photo-1504593811423-6dd665756598', '律师，平时健身和听播客，重视沟通效率与责任感。', 33, 1, '北京', '律师', 182,
       '["https://images.unsplash.com/photo-1504593811423-6dd665756598","https://images.unsplash.com/photo-1500648767791-00dcc994a43e"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC008', 'ENABLED', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_008');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '杜清禾', 'duqinghe@seed.lovecube.local', '13999010009', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_009',
       'https://images.unsplash.com/photo-1534528741775-53994a69daeb', '财务分析师，习惯计划生活，也愿意为关系投入时间。', 29, 2, '石家庄', '财务分析师', 166,
       '["https://images.unsplash.com/photo-1534528741775-53994a69daeb","https://images.unsplash.com/photo-1494790108377-be9c29b29330"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC009', 'ENABLED', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_009');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '吴致远', 'wuzhiyuan@seed.lovecube.local', '13999010010', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_010',
       'https://images.unsplash.com/photo-1519345182560-3f2917c472ef', '电气工程师，喜欢骑行和做饭，重视互相理解。', 27, 1, '保定', '电气工程师', 177,
       '["https://images.unsplash.com/photo-1519345182560-3f2917c472ef","https://images.unsplash.com/photo-1472099645785-5658abf4ff4e"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC010', 'ENABLED', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_010');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '冯可欣', 'fengkexin@seed.lovecube.local', '13999010011', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_011',
       'https://images.unsplash.com/photo-1488426862026-3ee34a7d66df', '品牌策划，关注精神共鸣和日常陪伴。', 28, 2, '北京', '品牌策划', 167,
       '["https://images.unsplash.com/photo-1488426862026-3ee34a7d66df","https://images.unsplash.com/photo-1487412720507-e7ab37603c6f"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC011', 'ENABLED', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_011');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '高一凡', 'gaoyifan@seed.lovecube.local', '13999010012', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_012',
       'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d', '从事公共服务工作，生活稳定，期待靠谱关系。', 30, 1, '石家庄', '公务员', 175,
       '["https://images.unsplash.com/photo-1506794778202-cad84cf45f1d","https://images.unsplash.com/photo-1519345182560-3f2917c472ef"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC012', 'ENABLED', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_012');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '张听澜', 'zhangtinglan@seed.lovecube.local', '13999010013', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_013',
       'https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e', '舞蹈教师，注重生活仪式感，也看重长期陪伴。', 26, 2, '保定', '舞蹈教师', 164,
       '["https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e","https://images.unsplash.com/photo-1488426862026-3ee34a7d66df"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC013', 'ENABLED', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_013');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '田嘉树', 'tianjiashu@seed.lovecube.local', '13999010014', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_014',
       'https://images.unsplash.com/photo-1463453091185-61582044d556', '互联网运营，喜欢城市探索和记录生活。', 32, 1, '北京', '互联网运营', 179,
       '["https://images.unsplash.com/photo-1463453091185-61582044d556","https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC014', 'ENABLED', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_014');

INSERT INTO users (username, email, phone_number, password_hash, openid, profile_photo, bio, age, gender, location, occupation, height, photos, vip_status, user_status, role, invite_code, invite_code_status, created_at, updated_at)
SELECT '马慧青', 'mahuiqing@seed.lovecube.local', '13999010015', '$2a$10$YvL8c4MSe9Y6w8S9F2r6mOhf7m9uQk7Qm9a9x8uFJ8pNwz0Jm1e2C', 'dev_seed_openid_015',
       'https://images.unsplash.com/photo-1544005313-94ddf0286df2', '医药代表，性格开朗，愿意认真了解彼此。', 31, 2, '石家庄', '医药代表', 166,
       '["https://images.unsplash.com/photo-1544005313-94ddf0286df2","https://images.unsplash.com/photo-1524504388940-b1c1722653e1"]',
       'normal', 'NORMAL', 'USER', 'DEVSEEDC015', 'ENABLED', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM users WHERE openid = 'dev_seed_openid_015');

-- 6) Fellowship profiles (15)
INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '陈子衡', 'male', 1997, 29, '保定', '建筑设计师', '本科', 178, '平时爱运动和看展，希望找到同频的生活搭子。', '以结婚为前提认真交往', u.profile_photo,
       '["跑步","纪录片","周末徒步"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_001'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '马慧琳', 'female', 1999, 27, '石家庄', '新媒体运营', '本科', 165, '喜欢展览和摄影，希望交流真诚轻松。', '先从稳定沟通开始，逐步了解', u.profile_photo,
       '["展览","摄影","手账"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_002'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '李沐阳', 'male', 1995, 31, '北京', '产品经理', '硕士', 180, '偏爱深度沟通，重视责任感与边界感。', '寻找价值观一致的长期关系', u.profile_photo,
       '["读书","播客","城市漫步"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_003'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '赵语晨', 'female', 2000, 26, '保定', '中学语文老师', '本科', 163, '喜欢阅读和写作，期待温和但坚定的关系。', '认真恋爱，稳定发展', u.profile_photo,
       '["阅读","写作","散步"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_004'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '孙嘉辰', 'male', 1998, 28, '石家庄', '软件工程师', '本科', 176, '性格理性，平时会健身和做饭。', '希望建立互相信任、可长期经营的关系', u.profile_photo,
       '["健身","做饭","科技"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_005'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '何思雨', 'female', 1996, 30, '北京', '室内设计师', '本科', 168, '喜欢艺术展与旅行，注重交流中的尊重感。', '以结婚为目标认真相处', u.profile_photo,
       '["设计","旅行","展览"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_006'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '郭安然', 'female', 2001, 25, '保定', '护士', '本科', 162, '生活规律，喜欢在休息日做甜品和看电影。', '希望找到踏实真诚的另一半', u.profile_photo,
       '["电影","甜品","慢生活"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_007'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '宋景川', 'male', 1993, 33, '北京', '律师', '硕士', 182, '重视诚信和执行力，期待双向奔赴。', '希望尽快进入稳定关系阶段', u.profile_photo,
       '["健身","法律","播客"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_008'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '杜清禾', 'female', 1997, 29, '石家庄', '财务分析师', '本科', 166, '喜欢有计划地安排生活，也愿意为关系留出弹性。', '期待成熟稳定、长期发展的关系', u.profile_photo,
       '["理财","阅读","羽毛球"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_009'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '吴致远', 'male', 1999, 27, '保定', '电气工程师', '本科', 177, '喜欢运动和烹饪，注重家庭责任感。', '认真恋爱并以婚姻为目标', u.profile_photo,
       '["骑行","烹饪","音乐"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_010'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '冯可欣', 'female', 1998, 28, '北京', '品牌策划', '本科', 167, '喜欢美术馆和纪录片，希望遇见同样真诚的人。', '希望先从高质量沟通开始', u.profile_photo,
       '["展览","纪录片","咖啡"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_011'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '高一凡', 'male', 1996, 30, '石家庄', '公务员', '本科', 175, '工作节奏稳定，喜欢登山和长距离散步。', '寻找能互相支持的长期伴侣', u.profile_photo,
       '["登山","散步","历史"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_012'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '张听澜', 'female', 2000, 26, '保定', '舞蹈教师', '本科', 164, '性格开朗，喜欢户外活动和城市市集。', '期待情绪稳定、互相欣赏的关系', u.profile_photo,
       '["舞蹈","市集","旅行"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_013'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '田嘉树', 'male', 1994, 32, '北京', '互联网运营', '本科', 179, '喜欢城市探索和摄影，期待共同成长。', '希望在半年内确定稳定关系', u.profile_photo,
       '["摄影","旅行","美食"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_014'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

INSERT INTO fellowship_profile (user_id, nickname, gender, birth_year, age, city, occupation, education, height, bio, intention, avatar_url, tags, identity_role, guardian_contact_visible, profile_status, review_status, created_at, updated_at)
SELECT u.userid, '马慧青', 'female', 1995, 31, '石家庄', '医药代表', '本科', 166, '工作中沟通较多，生活里更珍惜真诚和平静。', '认真建立长期关系，反对无效社交', u.profile_photo,
       '["旅行","羽毛球","电影"]', 'self', 1, 'COMPLETE', 'PENDING', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY
FROM users u WHERE u.openid = 'dev_seed_openid_015'
  AND NOT EXISTS (SELECT 1 FROM fellowship_profile fp WHERE fp.user_id = u.userid);

-- 7) Avatar / album photos (if table supports, here using user_photos)
INSERT INTO user_photos (user_id, photo_url, sort_order, is_primary, status, created_at, updated_at)
SELECT u.userid, u.profile_photo, 0, 1, 'ACTIVE', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY
FROM users u
WHERE u.openid LIKE 'dev_seed_openid_%'
  AND NOT EXISTS (
    SELECT 1 FROM user_photos p WHERE p.user_id = u.userid AND p.photo_url = u.profile_photo
  );

INSERT INTO user_photos (user_id, photo_url, sort_order, is_primary, status, created_at, updated_at)
SELECT u.userid,
       CONCAT('https://images.unsplash.com/photo-', 1500000000000 + u.userid, '?auto=format&fit=crop&w=900&q=80'),
       1, 0, 'ACTIVE', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY
FROM users u
WHERE u.openid LIKE 'dev_seed_openid_%'
  AND NOT EXISTS (
    SELECT 1 FROM user_photos p
    WHERE p.user_id = u.userid
      AND p.sort_order = 1
  );

COMMIT;
