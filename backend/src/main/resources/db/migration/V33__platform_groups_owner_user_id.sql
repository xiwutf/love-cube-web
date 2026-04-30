-- 团体拥有者（与 created_by 一致，便于查询与业务语义）
ALTER TABLE platform_groups
  ADD COLUMN owner_user_id BIGINT NULL COMMENT '团体拥有者用户ID' AFTER created_by;

UPDATE platform_groups
SET owner_user_id = created_by
WHERE owner_user_id IS NULL AND created_by IS NOT NULL;
