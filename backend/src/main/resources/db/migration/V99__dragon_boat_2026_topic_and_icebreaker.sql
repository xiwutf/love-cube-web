-- 2026 端午社区氛围：话题广场接龙 + 联谊动态官方破冰帖

INSERT INTO interest_topic (title, description, post_count, heat, sort_no, enabled)
SELECT
  '家乡端午怎么过',
  '挂艾草、赛龙舟、包粽子…你家乡有哪些小习俗？一两句即可，欢迎接龙。',
  0,
  20,
  0,
  1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM interest_topic WHERE title = '家乡端午怎么过'
);

INSERT INTO interest_topic_post (topic_id, user_id, content, like_count, status, created_at)
SELECT
  t.id,
  seed.userid,
  '我这边会包碱水粽，碱香一出来就知道端午到了。甜粽咸粽都可以——你家乡怎么吃？欢迎接龙 🌿',
  0,
  'published',
  NOW()
FROM interest_topic t
CROSS JOIN (
  SELECT u.userid
  FROM users u
  WHERE UPPER(u.role) IN ('ROOT', 'SUPER_ADMIN', 'ADMIN')
  ORDER BY FIELD(UPPER(u.role), 'ROOT', 'SUPER_ADMIN', 'ADMIN'), u.userid
  LIMIT 1
) seed
WHERE t.title = '家乡端午怎么过'
  AND seed.userid IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM interest_topic_post p
    INNER JOIN interest_topic t2 ON t2.id = p.topic_id
    WHERE t2.title = '家乡端午怎么过'
      AND p.content LIKE '我这边会包碱水粽%'
  );

UPDATE interest_topic t
SET
  post_count = (
    SELECT COUNT(*)
    FROM interest_topic_post p
    WHERE p.topic_id = t.id AND p.status = 'published'
  ),
  heat = GREATEST(COALESCE(t.heat, 0), 20)
WHERE t.title = '家乡端午怎么过';

INSERT INTO dynamics (
  user_id,
  content,
  image_urls,
  like_count,
  comment_count,
  share_count,
  is_deleted,
  scene_type,
  marker,
  created_at,
  updated_at
)
SELECT
  seed.userid,
  CONCAT(
    '端午假期来啦 🌿', CHAR(10), CHAR(10),
    '今天开启 #端午晒一晒：', CHAR(10),
    '· 粽子、家常、祝福、独处时光都算', CHAR(10),
    '· 不用精致，真实就好', CHAR(10), CHAR(10),
    '我先来：形状偶尔像石头，味道一直很像家。', CHAR(10),
    '#端午晒一晒 #丑粽子大赛', CHAR(10), CHAR(10),
    '敢晒的都不是完美主义者，欢迎跟帖～'
  ),
  NULL,
  0,
  0,
  0,
  0,
  'FELLOWSHIP',
  '[CAMPAIGN|DRAGON_BOAT_2026|ICEBREAKER]',
  NOW(),
  NOW()
FROM (
  SELECT u.userid
  FROM users u
  WHERE UPPER(u.role) IN ('ROOT', 'SUPER_ADMIN', 'ADMIN')
  ORDER BY FIELD(UPPER(u.role), 'ROOT', 'SUPER_ADMIN', 'ADMIN'), u.userid
  LIMIT 1
) seed
WHERE seed.userid IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM dynamics d
    WHERE d.marker = '[CAMPAIGN|DRAGON_BOAT_2026|ICEBREAKER]'
      AND d.is_deleted = 0
  );
