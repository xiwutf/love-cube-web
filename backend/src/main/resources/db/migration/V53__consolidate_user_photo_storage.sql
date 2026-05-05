-- 统一用户生活照存储：以 user_photos 为主，清理孤儿数据并回填历史 JSON 字段

-- 1) 清理 user_photos 中不存在用户的孤儿记录
DELETE p
FROM user_photos p
LEFT JOIN users u ON u.userid = p.user_id
WHERE u.userid IS NULL;

-- 2) 若用户在 user_photos 无记录，则从 users.photos(JSON 数组)回填
INSERT INTO user_photos (
    user_id,
    photo_url,
    sort_order,
    is_primary,
    status,
    created_at,
    updated_at
)
SELECT
    src.user_id,
    src.photo_url,
    src.sort_order,
    CASE WHEN src.sort_order = 0 THEN 1 ELSE 0 END AS is_primary,
    'ACTIVE' AS status,
    NOW() AS created_at,
    NOW() AS updated_at
FROM (
    SELECT
        u.userid AS user_id,
        jt.photo_url AS photo_url,
        jt.ord - 1 AS sort_order
    FROM users u
    JOIN JSON_TABLE(
        u.photos,
        '$[*]' COLUMNS (
            ord FOR ORDINALITY,
            photo_url VARCHAR(512) PATH '$'
        )
    ) jt
    WHERE
        u.photos IS NOT NULL
        AND TRIM(u.photos) <> ''
        AND TRIM(u.photos) <> '[]'
        AND JSON_VALID(u.photos)
        AND NOT EXISTS (
            SELECT 1 FROM user_photos p WHERE p.user_id = u.userid
        )
) src
WHERE src.photo_url IS NOT NULL AND TRIM(src.photo_url) <> '';

-- 3) users.photos 无法回填时，再用 fellowship_profiles.photos_json(JSON 数组)回填
INSERT INTO user_photos (
    user_id,
    photo_url,
    sort_order,
    is_primary,
    status,
    created_at,
    updated_at
)
SELECT
    src.user_id,
    src.photo_url,
    src.sort_order,
    CASE WHEN src.sort_order = 0 THEN 1 ELSE 0 END AS is_primary,
    'ACTIVE' AS status,
    NOW() AS created_at,
    NOW() AS updated_at
FROM (
    SELECT
        fp.user_id AS user_id,
        jt.photo_url AS photo_url,
        jt.ord - 1 AS sort_order
    FROM fellowship_profiles fp
    JOIN users u ON u.userid = fp.user_id
    JOIN JSON_TABLE(
        fp.photos_json,
        '$[*]' COLUMNS (
            ord FOR ORDINALITY,
            photo_url VARCHAR(512) PATH '$'
        )
    ) jt
    WHERE
        fp.photos_json IS NOT NULL
        AND TRIM(fp.photos_json) <> ''
        AND TRIM(fp.photos_json) <> '[]'
        AND JSON_VALID(fp.photos_json)
        AND NOT EXISTS (
            SELECT 1 FROM user_photos p WHERE p.user_id = fp.user_id
        )
) src
WHERE src.photo_url IS NOT NULL AND TRIM(src.photo_url) <> '';
