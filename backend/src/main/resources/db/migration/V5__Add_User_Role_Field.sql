-- 为 users 表新增 role 字段，并将历史管理员标记迁移到 role

-- 1) 新增 role 字段（兼容 MySQL 5.7，不使用 IF NOT EXISTS）
SET @db = DATABASE();
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT ''USER'' COMMENT ''用户角色：USER / ADMIN / SUPER_ADMIN / ROOT''',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 历史数据回填：优先根据 user_status 迁移
UPDATE users
SET role = CASE
    WHEN UPPER(COALESCE(user_status, '')) = 'ROOT' THEN 'ROOT'
    WHEN UPPER(COALESCE(user_status, '')) = 'SUPER_ADMIN' THEN 'SUPER_ADMIN'
    WHEN UPPER(COALESCE(user_status, '')) = 'ADMIN' THEN 'ADMIN'
    ELSE role
END
WHERE UPPER(COALESCE(user_status, '')) IN ('ADMIN', 'SUPER_ADMIN', 'ROOT');

-- 3) 历史兜底：兼容旧版手机号白名单管理员
UPDATE users
SET role = 'ADMIN'
WHERE phone_number IN ('13800000000', '15030251407')
  AND UPPER(COALESCE(role, '')) NOT IN ('ADMIN', 'SUPER_ADMIN', 'ROOT');

-- 4) 兜底清理：空值统一为 USER
UPDATE users
SET role = 'USER'
WHERE role IS NULL OR TRIM(role) = '';
