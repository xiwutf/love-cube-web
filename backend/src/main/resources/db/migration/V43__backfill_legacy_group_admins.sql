-- V39 把旧团体迁移进 platform_groups（id = 'legacy-{旧id}'），
-- 但没有为团体 owner 在 platform_group_admin 中写入记录，
-- 导致后台权限检查 requireGroupReviewPermission 始终找不到角色而返回 403。
-- 本迁移回填所有 legacy 团体 owner 的管理员记录，并补充后台入口角色码。
--
-- platform_groups.id          使用 utf8mb4_0900_ai_ci（MySQL 8 默认）
-- platform_group_admin.group_id 使用 utf8mb4_unicode_ci（V30 建表时指定）
-- 跨表字符串比较须显式统一 COLLATE，否则 MySQL 报 1267 错误。

-- 1. 为每个 legacy 团体的 owner 在 platform_group_admin 补记录
INSERT INTO platform_group_admin (group_id, user_id, role, created_at)
SELECT
    CONVERT(pg.id USING utf8mb4) COLLATE utf8mb4_unicode_ci AS group_id,
    pg.owner_user_id AS user_id,
    'OWNER'          AS role,
    NOW()            AS created_at
FROM platform_groups pg
WHERE pg.id LIKE 'legacy-%'
  AND pg.owner_user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM platform_group_admin pga
      WHERE pga.group_id = CONVERT(pg.id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        AND pga.user_id  = pg.owner_user_id
  );

-- 2. 确保这些 owner 拥有 GROUP_OWNER 角色码（后台访问入口）
INSERT INTO admin_user_role (user_id, role_code, created_at)
SELECT DISTINCT
    pg.owner_user_id AS user_id,
    'GROUP_OWNER'    AS role_code,
    NOW()            AS created_at
FROM platform_groups pg
WHERE pg.id LIKE 'legacy-%'
  AND pg.owner_user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM admin_user_role aur
      WHERE aur.user_id   = pg.owner_user_id
        AND aur.role_code = 'GROUP_OWNER'
  );
