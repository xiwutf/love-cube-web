-- 用户图片存储收敛校验清单（仅查询，不改数据）
-- 用途：在执行 V52 前后分别跑一遍，核对收敛结果。

-- =========================================
-- A. 基础盘点（执行前后都可）
-- =========================================

SELECT COUNT(*) AS users_total FROM users;

SELECT COUNT(*) AS users_photos_non_empty
FROM users
WHERE photos IS NOT NULL AND TRIM(photos) <> '' AND TRIM(photos) <> '[]';

SELECT COUNT(*) AS fellowship_profiles_photos_json_non_empty
FROM fellowship_profiles
WHERE photos_json IS NOT NULL AND TRIM(photos_json) <> '' AND TRIM(photos_json) <> '[]';

SELECT COUNT(DISTINCT user_id) AS user_photos_distinct_users
FROM user_photos;

SELECT COUNT(*) AS user_photos_active_rows
FROM user_photos
WHERE status = 'ACTIVE';

-- =========================================
-- B. 数据质量（重点看）
-- =========================================

-- 期望为 0
SELECT COUNT(DISTINCT p.user_id) AS user_photos_orphan_user_ids
FROM user_photos p
LEFT JOIN users u ON u.userid = p.user_id
WHERE u.userid IS NULL;

-- 期望尽量为 0
SELECT COUNT(*) AS legacy_only_users
FROM users u
WHERE
    (u.photos IS NOT NULL AND TRIM(u.photos) <> '' AND TRIM(u.photos) <> '[]')
    AND NOT EXISTS (
        SELECT 1 FROM user_photos p WHERE p.user_id = u.userid
    );

-- 若 legacy_only_users > 0，可看明细
SELECT
    u.userid,
    u.username,
    LEFT(u.photos, 120) AS photos_preview
FROM users u
WHERE
    (u.photos IS NOT NULL AND TRIM(u.photos) <> '' AND TRIM(u.photos) <> '[]')
    AND NOT EXISTS (
        SELECT 1 FROM user_photos p WHERE p.user_id = u.userid
    )
ORDER BY u.userid
LIMIT 200;

-- =========================================
-- C. 管理后台口径一致性
-- =========================================

SELECT COUNT(*) AS users_with_new_photos
FROM users u
WHERE EXISTS (SELECT 1 FROM user_photos p WHERE p.user_id = u.userid);

SELECT COUNT(*) AS users_without_new_photos
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM user_photos p WHERE p.user_id = u.userid);

-- 判定标准：
-- 1) orphan == 0
-- 2) legacy_only 尽量 == 0
-- 3) users_with_new_photos + users_without_new_photos == users_total
