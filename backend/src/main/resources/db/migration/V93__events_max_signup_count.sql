ALTER TABLE events
    ADD COLUMN max_signup_count INT NULL;

UPDATE events
SET max_signup_count = 40
WHERE id = 'event-baoding-dragonboat-2026';
