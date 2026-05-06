-- Remove demo / seed platform events and related signups; disable dev-seed home banners that linked to them.
DELETE FROM event_signups WHERE event_id IN (
    'dev-seed-event-01',
    'dev-seed-event-02',
    'dev-seed-event-03',
    'dev-seed-event-04',
    'dev-seed-event-05',
    'dev-seed-event-06',
    'dev-seed-event-07',
    'dev-seed-event-08',
    'online-icebreak-night',
    'interest-open-day',
    'offline-shanghai-meetup'
);

DELETE FROM events WHERE id IN (
    'dev-seed-event-01',
    'dev-seed-event-02',
    'dev-seed-event-03',
    'dev-seed-event-04',
    'dev-seed-event-05',
    'dev-seed-event-06',
    'dev-seed-event-07',
    'dev-seed-event-08',
    'online-icebreak-night',
    'interest-open-day',
    'offline-shanghai-meetup'
);

UPDATE home_configs
SET enabled = 0, updated_at = NOW()
WHERE config_group = 'home_banner'
  AND config_key IN ('dev_seed_home_banner_01', 'dev_seed_home_banner_02', 'dev_seed_home_banner_03');
