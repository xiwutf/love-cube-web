-- P0 成长系统修复：调整每日任务配置，补充评论任务

-- 统一每日任务奖励为 5 EXP / 项
UPDATE daily_tasks SET reward_exp = 5;

-- 点赞任务目标改为 3 次（符合 P0 规格）
UPDATE daily_tasks SET target_count = 3 WHERE code = 'DAILY_LIKE';

-- 浏览任务目标改为 10 条
UPDATE daily_tasks SET target_count = 10 WHERE code = 'DAILY_VIEW';

-- 重排 sort_no，为新增评论任务留位
UPDATE daily_tasks SET sort_no = 1 WHERE code = 'DAILY_LOGIN';
UPDATE daily_tasks SET sort_no = 2 WHERE code = 'DAILY_POST';
UPDATE daily_tasks SET sort_no = 4 WHERE code = 'DAILY_VIEW';
UPDATE daily_tasks SET sort_no = 5 WHERE code = 'DAILY_LIKE';

-- 新增：评论 1 次每日任务
INSERT INTO daily_tasks (code, name, action_type, target_count, reward_exp, sort_no)
VALUES ('DAILY_COMMENT', '评论1次', 'COMMENT_CONTENT', 1, 5, 3);
