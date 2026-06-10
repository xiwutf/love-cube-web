-- 联谊滑卡撤回：每日次数记录在 user_daily_swipe
ALTER TABLE user_daily_swipe
    ADD COLUMN rewind_count INT NOT NULL DEFAULT 0 AFTER swipe_count;
