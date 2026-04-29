-- Seed platform_group_post: 5 posts spread across groups
-- CROSS JOIN (SELECT userid ...) ensures rows only insert if a user exists

INSERT INTO platform_group_post (group_id, user_id, content, status, created_at, updated_at)
SELECT g.id, u.userid,
       '上周我们在朝阳公园举办了团契户外活动，弟兄姊妹一起游戏、祷告、分享见证，气氛非常温馨！感谢主的带领，期待下次相聚！',
       'published', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'beijing-youth';

INSERT INTO platform_group_post (group_id, user_id, content, status, created_at, updated_at)
SELECT g.id, u.userid,
       '本周五晚上7点，线上查经分享会准时开始，主题是诗篇第23篇。欢迎弟兄姊妹参加，带上你的思考与见证，一起在神的话语中得力！',
       'published', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'beijing-youth';

INSERT INTO platform_group_post (group_id, user_id, content, status, created_at, updated_at)
SELECT g.id, u.userid,
       '本周日将举行爱宴，请各位弟兄姊妹积极准备菜品参与，大家一起共度美好时光，彼此相爱，分享恩典。地点：礼堂侧厅。',
       'published', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'shanghai-church';

INSERT INTO platform_group_post (group_id, user_id, content, status, created_at, updated_at)
SELECT g.id, u.userid,
       '今天我们共同分享了约翰福音第15章——葡萄树与枝子的比喻，让我们深刻体会到与主连接的重要性。欢迎大家加入我们的读经之旅！',
       'published', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'reading-share';

INSERT INTO platform_group_post (group_id, user_id, content, status, created_at, updated_at)
SELECT g.id, u.userid,
       '敬拜团新曲排练顺利完成，感谢每一位弟兄姊妹的付出与坚持。本周主日我们将带领大家一同歌颂，愿赞美声充满礼堂！',
       'published', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'worship-band';

-- Seed platform_group_notice: 3 notices

INSERT INTO platform_group_notice (group_id, title, content, created_by, status, created_at, updated_at)
SELECT g.id,
       '第二季度活动安排公告',
       '各位弟兄姊妹，第二季度团契活动安排如下：4月第三周户外联谊、5月第二周读经营、6月第一周感恩聚会。具体时间地点另行通知，请大家留意群消息。',
       u.userid, 'published', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'beijing-youth';

INSERT INTO platform_group_notice (group_id, title, content, created_by, status, created_at, updated_at)
SELECT g.id,
       '下半年聚会时间调整通知',
       '自下月起，主日崇拜时间调整为上午9:30开始，请各位弟兄姊妹提前安排好行程，准时到场。如有特殊情况请联系执事团队。',
       u.userid, 'published', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'shanghai-church';

INSERT INTO platform_group_notice (group_id, title, content, created_by, status, created_at, updated_at)
SELECT g.id,
       '本月读经进度与主题提醒',
       '本月读经进度：第一、二周完成马太福音1-14章，第三、四周完成15-28章。每周六晚有线上分享，欢迎弟兄姊妹积极参与，分享心得。',
       u.userid, 'published', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM platform_group g
CROSS JOIN (SELECT userid FROM users ORDER BY userid LIMIT 1) u
WHERE g.slug = 'reading-share';
