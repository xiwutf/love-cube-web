ALTER TABLE events
    ADD COLUMN end_time DATETIME NULL;

UPDATE events
SET end_time = DATE_ADD(event_time, INTERVAL 3 HOUR)
WHERE end_time IS NULL AND event_time IS NOT NULL;

UPDATE events
SET end_time = '2026-06-19 17:30:00'
WHERE id = 'event-baoding-dragonboat-2026';
