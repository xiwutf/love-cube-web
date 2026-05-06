-- 字段治理：可重复执行的数据回填（不删除列、不破坏已有数据）
-- 1) 联谊主表与旧 fellowship_profile 头像互填（仅当目标为空且源有值）
-- 2) platform_groups 拥有者兜底（与 V33 一致，幂等）

UPDATE fellowship_profiles fp
INNER JOIN fellowship_profile leg ON leg.user_id = fp.user_id
SET fp.avatar = leg.avatar_url
WHERE (fp.avatar IS NULL OR TRIM(fp.avatar) = '')
  AND leg.avatar_url IS NOT NULL AND TRIM(leg.avatar_url) <> '';

UPDATE fellowship_profile leg
INNER JOIN fellowship_profiles fp ON fp.user_id = leg.user_id
SET leg.avatar_url = fp.avatar
WHERE (leg.avatar_url IS NULL OR TRIM(leg.avatar_url) = '')
  AND fp.avatar IS NOT NULL AND TRIM(fp.avatar) <> '';

UPDATE platform_groups
SET owner_user_id = created_by
WHERE owner_user_id IS NULL AND created_by IS NOT NULL;
