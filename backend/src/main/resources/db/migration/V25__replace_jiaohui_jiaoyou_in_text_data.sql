-- 将各表文本字段中的「教会」「教友」替换为单个空格（含历史数据与用户生成内容）

UPDATE users SET bio = REPLACE(REPLACE(bio, '教会', ' '), '教友', ' ')
WHERE bio IS NOT NULL AND (bio LIKE '%教会%' OR bio LIKE '%教友%');

UPDATE chatmessages SET content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' ')
WHERE content LIKE '%教会%' OR content LIKE '%教友%';

UPDATE user_profiles SET
  nickname = REPLACE(REPLACE(nickname, '教会', ' '), '教友', ' '),
  tag = REPLACE(REPLACE(tag, '教会', ' '), '教友', ' '),
  education = REPLACE(REPLACE(education, '教会', ' '), '教友', ' '),
  city = REPLACE(REPLACE(city, '教会', ' '), '教友', ' '),
  province = REPLACE(REPLACE(province, '教会', ' '), '教友', ' ')
WHERE (nickname IS NOT NULL AND (nickname LIKE '%教会%' OR nickname LIKE '%教友%'))
   OR (tag IS NOT NULL AND (tag LIKE '%教会%' OR tag LIKE '%教友%'))
   OR (education IS NOT NULL AND (education LIKE '%教会%' OR education LIKE '%教友%'))
   OR (city IS NOT NULL AND (city LIKE '%教会%' OR city LIKE '%教友%'))
   OR (province IS NOT NULL AND (province LIKE '%教会%' OR province LIKE '%教友%'));

UPDATE fellowship_profiles SET
  nickname = REPLACE(REPLACE(nickname, '教会', ' '), '教友', ' '),
  bio = REPLACE(REPLACE(bio, '教会', ' '), '教友', ' '),
  verification_note = REPLACE(REPLACE(verification_note, '教会', ' '), '教友', ' '),
  review_note = REPLACE(REPLACE(review_note, '教会', ' '), '教友', ' ')
WHERE (nickname IS NOT NULL AND (nickname LIKE '%教会%' OR nickname LIKE '%教友%'))
   OR (bio IS NOT NULL AND (bio LIKE '%教会%' OR bio LIKE '%教友%'))
   OR (verification_note IS NOT NULL AND (verification_note LIKE '%教会%' OR verification_note LIKE '%教友%'))
   OR (review_note IS NOT NULL AND (review_note LIKE '%教会%' OR review_note LIKE '%教友%'));

UPDATE fellowship_profile SET
  nickname = REPLACE(REPLACE(nickname, '教会', ' '), '教友', ' '),
  bio = REPLACE(REPLACE(bio, '教会', ' '), '教友', ' '),
  intention = REPLACE(REPLACE(intention, '教会', ' '), '教友', ' '),
  tags = REPLACE(REPLACE(tags, '教会', ' '), '教友', ' '),
  child_marriage_intention = REPLACE(REPLACE(child_marriage_intention, '教会', ' '), '教友', ' '),
  child_partner_requirements = REPLACE(REPLACE(child_partner_requirements, '教会', ' '), '教友', ' ')
WHERE (nickname IS NOT NULL AND (nickname LIKE '%教会%' OR nickname LIKE '%教友%'))
   OR (bio IS NOT NULL AND (bio LIKE '%教会%' OR bio LIKE '%教友%'))
   OR (intention IS NOT NULL AND (intention LIKE '%教会%' OR intention LIKE '%教友%'))
   OR (tags IS NOT NULL AND (tags LIKE '%教会%' OR tags LIKE '%教友%'))
   OR (child_marriage_intention IS NOT NULL AND (child_marriage_intention LIKE '%教会%' OR child_marriage_intention LIKE '%教友%'))
   OR (child_partner_requirements IS NOT NULL AND (child_partner_requirements LIKE '%教会%' OR child_partner_requirements LIKE '%教友%'));

UPDATE user_tags SET tag = REPLACE(REPLACE(tag, '教会', ' '), '教友', ' ')
WHERE tag LIKE '%教会%' OR tag LIKE '%教友%';

-- 以下三张表由历史 schema 或运维创建，未必存在于纯 Flyway 环境
SET @db := DATABASE();

SET @q := IF(
  (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @db AND table_name = 'announcements') > 0,
  'UPDATE announcements SET title = REPLACE(REPLACE(title, ''教会'', '' ''), ''教友'', '' ''), summary = REPLACE(REPLACE(summary, ''教会'', '' ''), ''教友'', '' ''), content = REPLACE(REPLACE(content, ''教会'', '' ''), ''教友'', '' '') WHERE title LIKE ''%教会%'' OR title LIKE ''%教友%'' OR (summary IS NOT NULL AND (summary LIKE ''%教会%'' OR summary LIKE ''%教友%'')) OR (content IS NOT NULL AND (content LIKE ''%教会%'' OR content LIKE ''%教友%''))',
  'SELECT 1');
PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @q := IF(
  (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @db AND table_name = 'articles') > 0,
  'UPDATE articles SET tag = REPLACE(REPLACE(tag, ''教会'', '' ''), ''教友'', '' ''), title = REPLACE(REPLACE(title, ''教会'', '' ''), ''教友'', '' ''), summary = REPLACE(REPLACE(summary, ''教会'', '' ''), ''教友'', '' ''), content = REPLACE(REPLACE(content, ''教会'', '' ''), ''教友'', '' '') WHERE (tag IS NOT NULL AND (tag LIKE ''%教会%'' OR tag LIKE ''%教友%'')) OR title LIKE ''%教会%'' OR title LIKE ''%教友%'' OR (summary IS NOT NULL AND (summary LIKE ''%教会%'' OR summary LIKE ''%教友%'')) OR (content IS NOT NULL AND (content LIKE ''%教会%'' OR content LIKE ''%教友%''))',
  'SELECT 1');
PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @q := IF(
  (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @db AND table_name = 'events') > 0,
  'UPDATE events SET title = REPLACE(REPLACE(title, ''教会'', '' ''), ''教友'', '' ''), summary = REPLACE(REPLACE(summary, ''教会'', '' ''), ''教友'', '' ''), content = REPLACE(REPLACE(content, ''教会'', '' ''), ''教友'', '' ''), location = REPLACE(REPLACE(location, ''教会'', '' ''), ''教友'', '' '') WHERE title LIKE ''%教会%'' OR title LIKE ''%教友%'' OR (summary IS NOT NULL AND (summary LIKE ''%教会%'' OR summary LIKE ''%教友%'')) OR (content IS NOT NULL AND (content LIKE ''%教会%'' OR content LIKE ''%教友%'')) OR (location IS NOT NULL AND (location LIKE ''%教会%'' OR location LIKE ''%教友%''))',
  'SELECT 1');
PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE reports SET
  content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' '),
  note = REPLACE(REPLACE(note, '教会', ' '), '教友', ' ')
WHERE (content IS NOT NULL AND (content LIKE '%教会%' OR content LIKE '%教友%'))
   OR (note IS NOT NULL AND (note LIKE '%教会%' OR note LIKE '%教友%'));

UPDATE verification_requests SET
  note = REPLACE(REPLACE(note, '教会', ' '), '教友', ' '),
  reject_reason = REPLACE(REPLACE(reject_reason, '教会', ' '), '教友', ' ')
WHERE (note IS NOT NULL AND (note LIKE '%教会%' OR note LIKE '%教友%'))
   OR (reject_reason IS NOT NULL AND (reject_reason LIKE '%教会%' OR reject_reason LIKE '%教友%'));

UPDATE user_verifications SET reject_reason = REPLACE(REPLACE(reject_reason, '教会', ' '), '教友', ' ')
WHERE reject_reason IS NOT NULL AND (reject_reason LIKE '%教会%' OR reject_reason LIKE '%教友%');

UPDATE notifications SET
  title = REPLACE(REPLACE(title, '教会', ' '), '教友', ' '),
  content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' ')
WHERE title LIKE '%教会%' OR title LIKE '%教友%'
   OR content LIKE '%教会%' OR content LIKE '%教友%';

UPDATE positive_share SET content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' ')
WHERE content LIKE '%教会%' OR content LIKE '%教友%';

UPDATE positive_share_comment SET content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' ')
WHERE content LIKE '%教会%' OR content LIKE '%教友%';

UPDATE home_configs SET config_value = REPLACE(REPLACE(config_value, '教会', ' '), '教友', ' ')
WHERE config_value LIKE '%教会%' OR config_value LIKE '%教友%';

UPDATE invite_record SET invitee_username = REPLACE(REPLACE(invitee_username, '教会', ' '), '教友', ' ')
WHERE invitee_username IS NOT NULL AND (invitee_username LIKE '%教会%' OR invitee_username LIKE '%教友%');

UPDATE platform_groups SET
  name = REPLACE(REPLACE(name, '教会', ' '), '教友', ' '),
  description = REPLACE(REPLACE(description, '教会', ' '), '教友', ' ')
WHERE name LIKE '%教会%' OR name LIKE '%教友%'
   OR (description IS NOT NULL AND (description LIKE '%教会%' OR description LIKE '%教友%'));

UPDATE group_posts SET content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' ')
WHERE content LIKE '%教会%' OR content LIKE '%教友%';

UPDATE group_join_requests SET message = REPLACE(REPLACE(message, '教会', ' '), '教友', ' ')
WHERE message IS NOT NULL AND (message LIKE '%教会%' OR message LIKE '%教友%');

UPDATE platform_group SET
  name = REPLACE(REPLACE(name, '教会', ' '), '教友', ' '),
  description = REPLACE(REPLACE(description, '教会', ' '), '教友', ' ')
WHERE name LIKE '%教会%' OR name LIKE '%教友%'
   OR (description IS NOT NULL AND (description LIKE '%教会%' OR description LIKE '%教友%'));

UPDATE platform_group_post SET content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' ')
WHERE content LIKE '%教会%' OR content LIKE '%教友%';

UPDATE platform_group_notice SET
  title = REPLACE(REPLACE(title, '教会', ' '), '教友', ' '),
  content = REPLACE(REPLACE(content, '教会', ' '), '教友', ' ')
WHERE title LIKE '%教会%' OR title LIKE '%教友%'
   OR (content IS NOT NULL AND (content LIKE '%教会%' OR content LIKE '%教友%'));

UPDATE platform_group_member SET apply_reason = REPLACE(REPLACE(apply_reason, '教会', ' '), '教友', ' ')
WHERE apply_reason IS NOT NULL AND (apply_reason LIKE '%教会%' OR apply_reason LIKE '%教友%');
