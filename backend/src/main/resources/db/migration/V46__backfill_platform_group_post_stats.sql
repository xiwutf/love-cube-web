-- 已有动态的团体：补齐 post_count / last_active_at（V45 新增字段默认为 0 / NULL）
UPDATE platform_group g
SET post_count = (
        SELECT COUNT(*)
        FROM platform_group_post p
        WHERE p.group_id = g.id
          AND p.status = 'published'
    ),
    last_active_at = (
        SELECT MAX(p.created_at)
        FROM platform_group_post p
        WHERE p.group_id = g.id
          AND p.status = 'published'
    )
WHERE EXISTS (
    SELECT 1 FROM platform_group_post p WHERE p.group_id = g.id AND p.status = 'published'
);
