-- 将旧的空值或 GROUP_OWNER 统一为 OWNER
UPDATE platform_group_admin
SET role = 'OWNER'
WHERE role IS NULL OR role = '' OR role = 'GROUP_OWNER';
