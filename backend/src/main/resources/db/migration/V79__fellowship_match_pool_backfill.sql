-- 已开通联谊但未打开匹配可见的用户补全（与 V22 一致，防止漏网）
UPDATE users
SET fellowship_match_visible = 1
WHERE fellowship_enabled = 1
  AND (fellowship_match_visible IS NULL OR fellowship_match_visible = 0);

-- 已在联谊动态流发过内容、资料基本完整的用户，默认进入匹配/推荐池
UPDATE users u
INNER JOIN (
    SELECT DISTINCT d.user_id
    FROM dynamics d
    WHERE d.is_deleted = 0
      AND d.scene_type = 'FELLOWSHIP'
) active ON active.user_id = u.userid
SET u.fellowship_enabled = 1,
    u.fellowship_match_visible = 1
WHERE (u.user_status IS NULL OR LOWER(u.user_status) <> 'disabled')
  AND u.gender IN (1, 2)
  AND u.profile_photo IS NOT NULL
  AND TRIM(u.profile_photo) <> '';
