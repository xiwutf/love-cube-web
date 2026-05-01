-- V43 只回填了 owner_user_id IS NOT NULL 的 legacy 团体。
-- 若 owner_user_id 为 NULL 但 created_by 有值，团长仍缺少 platform_group_admin 记录。
-- 本迁移以 created_by 为兜底，补全剩余情况，并同步 owner_user_id 字段与后台角色。

-- 1. 以 created_by 为 owner_user_id 的兜底插入 platform_group_admin
INSERT INTO platform_group_admin (group_id, user_id, role, created_at)
SELECT
    CONVERT(pg.id USING utf8mb4) COLLATE utf8mb4_unicode_ci AS group_id,
    pg.created_by                                           AS user_id,
    'OWNER'                                                 AS role,
    NOW()                                                   AS created_at
FROM platform_groups pg
WHERE pg.id LIKE 'legacy-%'
  AND pg.owner_user_id IS NULL
  AND pg.created_by IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM platform_group_admin pga
      WHERE pga.group_id = CONVERT(pg.id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        AND pga.user_id  = pg.created_by
  );

-- 2. 将 owner_user_id 补齐为 created_by（保持字段一致）
UPDATE platform_groups
SET owner_user_id = created_by
WHERE id LIKE 'legacy-%'
  AND owner_user_id IS NULL
  AND created_by IS NOT NULL;

-- 3. 确保所有 legacy 团体管理员都拥有 GROUP_OWNER 后台入口角色码
INSERT INTO admin_user_role (user_id, role_code, created_at)
SELECT DISTINCT pga.user_id, 'GROUP_OWNER', NOW()
FROM platform_group_admin pga
WHERE pga.group_id LIKE 'legacy-%'
  AND NOT EXISTS (
      SELECT 1
      FROM admin_user_role aur
      WHERE aur.user_id   = pga.user_id
        AND aur.role_code = 'GROUP_OWNER'
  );
