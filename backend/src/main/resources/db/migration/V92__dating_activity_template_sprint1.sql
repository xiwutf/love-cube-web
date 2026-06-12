ALTER TABLE events
    ADD COLUMN template_type VARCHAR(32) NOT NULL DEFAULT 'GENERAL';

CREATE TABLE dating_event_identity (
    event_id    VARCHAR(64)  NOT NULL,
    user_id     BIGINT       NOT NULL,
    gender_side VARCHAR(16)  NOT NULL,
    badge_seq   INT          NOT NULL,
    badge_label VARCHAR(16)  NOT NULL,
    assigned_at DATETIME     NOT NULL,
    PRIMARY KEY (event_id, user_id),
    UNIQUE KEY uk_dating_event_badge (event_id, badge_label),
    KEY idx_dating_event_side (event_id, gender_side, badge_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dating_event_profile (
    event_id              VARCHAR(64)  NOT NULL,
    user_id               BIGINT       NOT NULL,
    age                   INT          NULL,
    height_cm             INT          NULL,
    city                  VARCHAR(64)  NULL,
    occupation            VARCHAR(128) NULL,
    education             VARCHAR(64)  NULL,
    interest_tags         TEXT         NULL,
    self_intro            TEXT         NULL,
    partner_requirements  TEXT         NULL,
    ideal_partner_desc    TEXT         NULL,
    completed             TINYINT(1)   NOT NULL DEFAULT 0,
    created_at            DATETIME     NOT NULL,
    updated_at            DATETIME     NOT NULL,
    PRIMARY KEY (event_id, user_id),
    KEY idx_dating_profile_event (event_id, completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE events
SET template_type = 'DATING'
WHERE id = 'event-baoding-dragonboat-2026';
