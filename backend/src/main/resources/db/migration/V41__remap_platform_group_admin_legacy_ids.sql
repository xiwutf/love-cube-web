-- V39 migrated platform_group rows into platform_groups with id = CONCAT('legacy-', old_id).
-- platform_group_admin.group_id was left as the old numeric string, so
-- requireGroupReviewPermission(auth, 'legacy-20') never matches rows with group_id '20'.
-- Remap only when the legacy row exists and no platform_groups row still uses the bare numeric id.

UPDATE platform_group_admin pga
INNER JOIN platform_groups lg
  ON CONVERT(lg.id USING utf8mb4) COLLATE utf8mb4_unicode_ci
   = CONCAT('legacy-', CONVERT(pga.group_id USING utf8mb4) COLLATE utf8mb4_unicode_ci)
SET pga.group_id = lg.id
WHERE pga.group_id NOT LIKE 'legacy-%'
  AND pga.group_id REGEXP '^[0-9]+$'
  AND NOT EXISTS (
    SELECT 1 FROM platform_groups g
    WHERE CONVERT(g.id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        = CONVERT(pga.group_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
  );
