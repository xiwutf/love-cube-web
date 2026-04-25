-- 手动添加缺失的字段到users表
-- 执行这个脚本来修复数据库结构

-- 检查并添加height字段
SELECT COUNT(*) INTO @height_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'height';

SET @sql = IF(@height_exists = 0, 
    'ALTER TABLE users ADD COLUMN height INT NULL COMMENT "用户身高(厘米)"', 
    'SELECT "height字段已存在" as result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加photos字段
SELECT COUNT(*) INTO @photos_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'photos';

SET @sql = IF(@photos_exists = 0, 
    'ALTER TABLE users ADD COLUMN photos TEXT NULL COMMENT "生活照片JSON数组"', 
    'SELECT "photos字段已存在" as result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 vip_status 字段（与 JPA User 实体一致）
SELECT COUNT(*) INTO @vip_status_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'vip_status';

SET @sql = IF(@vip_status_exists = 0,
    'ALTER TABLE users ADD COLUMN vip_status VARCHAR(20) NULL DEFAULT ''none'' COMMENT ''VIP: none/month/season/year''',
    'SELECT "vip_status字段已存在" as result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 vip_expires_at 字段
SELECT COUNT(*) INTO @vip_expires_at_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'vip_expires_at';

SET @sql = IF(@vip_expires_at_exists = 0,
    'ALTER TABLE users ADD COLUMN vip_expires_at DATETIME(6) NULL COMMENT ''VIP 到期时间''',
    'SELECT "vip_expires_at字段已存在" as result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;