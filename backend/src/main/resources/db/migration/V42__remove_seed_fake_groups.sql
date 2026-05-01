-- Remove seeded fake groups from legacy + migrated new tables.
-- Targets (by legacy slug):
-- beijing-youth, shanghai-church, guangzhou-pray, reading-share, worship-band, family-fellowship
-- (Split from duplicate V40; runs after V41.)

CREATE TEMPORARY TABLE tmp_seed_group_ids AS
SELECT id
FROM platform_group
WHERE slug IN (
  'beijing-youth',
  'shanghai-church',
  'guangzhou-pray',
  'reading-share',
  'worship-band',
  'family-fellowship'
);

-- New tables (migrated by V39 using id format: legacy-{oldId})
DELETE FROM group_join_requests
WHERE group_id IN (
  SELECT CONCAT('legacy-', id) FROM tmp_seed_group_ids
);

DELETE FROM group_posts
WHERE group_id IN (
  SELECT CONCAT('legacy-', id) FROM tmp_seed_group_ids
);

DELETE FROM group_members
WHERE group_id IN (
  SELECT CONCAT('legacy-', id) FROM tmp_seed_group_ids
);

DELETE FROM platform_groups
WHERE id IN (
  SELECT CONCAT('legacy-', id) FROM tmp_seed_group_ids
);

-- Legacy tables
DELETE FROM platform_group_notice
WHERE group_id IN (SELECT id FROM tmp_seed_group_ids);

DELETE FROM platform_group_post
WHERE group_id IN (SELECT id FROM tmp_seed_group_ids);

DELETE FROM platform_group_member
WHERE group_id IN (SELECT id FROM tmp_seed_group_ids);

DELETE FROM platform_group
WHERE id IN (SELECT id FROM tmp_seed_group_ids);

DROP TEMPORARY TABLE tmp_seed_group_ids;
