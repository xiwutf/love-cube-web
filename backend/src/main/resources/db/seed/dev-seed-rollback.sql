-- Rollback for dev seed data
-- Safety: only removes rows inserted by dev seed markers

SET NAMES utf8mb4;

START TRANSACTION;

-- 1) Fellowship related data (child tables first)
DELETE p
FROM user_photos p
JOIN users u ON u.userid = p.user_id
WHERE u.openid LIKE 'dev_seed_openid_%';

DELETE fp
FROM fellowship_profile fp
JOIN users u ON u.userid = fp.user_id
WHERE u.openid LIKE 'dev_seed_openid_%';

DELETE FROM users
WHERE openid LIKE 'dev_seed_openid_%'
   OR invite_code LIKE 'DEVSEEDC%';

-- 2) Home configs
DELETE FROM home_configs
WHERE config_group = 'home_banner'
  AND config_key LIKE 'dev_seed_home_banner_%';

-- 3) Platform content
DELETE FROM events
WHERE id LIKE 'dev-seed-event-%';

DELETE FROM articles
WHERE id LIKE 'dev-seed-article-%';

DELETE FROM announcements
WHERE id LIKE 'dev-seed-announcement-%';

COMMIT;
