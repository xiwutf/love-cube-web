-- Migrate legacy platform group data from:
-- platform_group / platform_group_member / platform_group_post / platform_group_notice
-- to:
-- platform_groups / group_members / group_posts / group_join_requests

-- 1) Groups
INSERT INTO platform_groups (
  id,
  name,
  description,
  cover_url,
  category,
  status,
  join_type,
  member_count,
  pinned,
  created_by,
  owner_user_id,
  created_at,
  updated_at
)
SELECT
  CONCAT('legacy-', pg.id) AS id,
  pg.name,
  pg.description,
  pg.cover_url,
  pg.type,
  CASE WHEN pg.status = 'published' THEN 'active' ELSE COALESCE(pg.status, 'inactive') END AS status,
  CASE WHEN pg.join_mode = 'free' THEN 'open' ELSE 'approval' END AS join_type,
  COALESCE(pg.member_count, 0) AS member_count,
  0 AS pinned,
  pg.owner_user_id AS created_by,
  pg.owner_user_id,
  COALESCE(pg.created_at, NOW()) AS created_at,
  COALESCE(pg.updated_at, NOW()) AS updated_at
FROM platform_group pg
WHERE NOT EXISTS (
  SELECT 1
  FROM platform_groups ng
  WHERE ng.id = CONCAT('legacy-', pg.id)
);

-- 2) Approved members -> group_members
INSERT INTO group_members (
  group_id,
  user_id,
  role,
  joined_at
)
SELECT
  CONCAT('legacy-', m.group_id) AS group_id,
  m.user_id,
  CASE WHEN m.role IN ('owner', 'admin', 'member') THEN m.role ELSE 'member' END AS role,
  COALESCE(m.joined_at, m.updated_at, m.created_at, NOW()) AS joined_at
FROM platform_group_member m
JOIN platform_groups g ON g.id = CONCAT('legacy-', m.group_id)
WHERE m.status = 'approved'
  AND NOT EXISTS (
    SELECT 1
    FROM group_members gm
    WHERE gm.group_id = CONCAT('legacy-', m.group_id)
      AND gm.user_id = m.user_id
  );

-- 3) Pending/rejected applications -> group_join_requests
INSERT INTO group_join_requests (
  group_id,
  user_id,
  status,
  message,
  requested_at,
  handled_at
)
SELECT
  CONCAT('legacy-', m.group_id) AS group_id,
  m.user_id,
  CASE
    WHEN m.status = 'pending' THEN 'pending'
    WHEN m.status = 'rejected' THEN 'rejected'
    ELSE 'pending'
  END AS status,
  m.apply_reason AS message,
  COALESCE(m.created_at, m.updated_at, NOW()) AS requested_at,
  CASE WHEN m.status = 'rejected' THEN COALESCE(m.updated_at, NOW()) ELSE NULL END AS handled_at
FROM platform_group_member m
JOIN platform_groups g ON g.id = CONCAT('legacy-', m.group_id)
WHERE m.status IN ('pending', 'rejected')
  AND NOT EXISTS (
    SELECT 1
    FROM group_join_requests gjr
    WHERE gjr.group_id = CONCAT('legacy-', m.group_id)
      AND gjr.user_id = m.user_id
      AND gjr.status = CASE
        WHEN m.status = 'pending' THEN 'pending'
        WHEN m.status = 'rejected' THEN 'rejected'
        ELSE 'pending'
      END
  );

-- 4) Legacy posts -> group_posts
INSERT INTO group_posts (
  id,
  group_id,
  user_id,
  type,
  content,
  like_count,
  created_at,
  updated_at
)
SELECT
  CONCAT('legacy-post-', p.id) AS id,
  CONCAT('legacy-', p.group_id) AS group_id,
  p.user_id,
  'post' AS type,
  p.content,
  0 AS like_count,
  COALESCE(p.created_at, NOW()) AS created_at,
  COALESCE(p.updated_at, NOW()) AS updated_at
FROM platform_group_post p
JOIN platform_groups g ON g.id = CONCAT('legacy-', p.group_id)
WHERE p.status = 'published'
  AND NOT EXISTS (
    SELECT 1
    FROM group_posts gp
    WHERE gp.id = CONCAT('legacy-post-', p.id)
  );

-- 5) Legacy notices -> group_posts(type=notice)
INSERT INTO group_posts (
  id,
  group_id,
  user_id,
  type,
  content,
  like_count,
  created_at,
  updated_at
)
SELECT
  CONCAT('legacy-notice-', n.id) AS id,
  CONCAT('legacy-', n.group_id) AS group_id,
  COALESCE(n.created_by, pg.owner_user_id, 0) AS user_id,
  'notice' AS type,
  CONCAT('[', n.title, '] ', COALESCE(n.content, '')) AS content,
  0 AS like_count,
  COALESCE(n.created_at, NOW()) AS created_at,
  COALESCE(n.updated_at, NOW()) AS updated_at
FROM platform_group_notice n
JOIN platform_group pg ON pg.id = n.group_id
JOIN platform_groups g ON g.id = CONCAT('legacy-', n.group_id)
WHERE n.status = 'published'
  AND NOT EXISTS (
    SELECT 1
    FROM group_posts gp
    WHERE gp.id = CONCAT('legacy-notice-', n.id)
  );

-- 6) Recalculate migrated groups member_count by approved members
UPDATE platform_groups g
SET g.member_count = (
  SELECT COUNT(1)
  FROM group_members gm
  WHERE gm.group_id = g.id
)
WHERE g.id LIKE 'legacy-%';
