-- 已开通联谊的用户默认应出现在匹配池：历史上仅写了 fellowship_enabled，未打开 fellowship_match_visible

UPDATE users
SET fellowship_match_visible = 1
WHERE fellowship_enabled = 1
  AND (fellowship_match_visible IS NULL OR fellowship_match_visible = 0);
