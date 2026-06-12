-- Recover from a failed prior V94 attempt (MySQL auto-commits each DDL statement).
DROP TABLE IF EXISTS event_guest_participant;

SET @guest_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'event_signups'
      AND COLUMN_NAME = 'guest_participant_id'
);
SET @sql := IF(@guest_col > 0,
    'ALTER TABLE event_signups DROP INDEX uk_event_signups_event_guest, DROP COLUMN guest_participant_id, MODIFY user_id BIGINT NOT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @identity_row := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'dating_event_identity'
      AND COLUMN_NAME = 'row_id'
);
SET @sql := IF(@identity_row > 0,
    'ALTER TABLE dating_event_identity DROP COLUMN row_id, DROP COLUMN guest_participant_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE event_guest_participant (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    event_id            VARCHAR(64)  NOT NULL,
    guest_token         VARCHAR(64)  NOT NULL,
    nickname            VARCHAR(64)  NOT NULL,
    gender_side         VARCHAR(16)  NOT NULL,
    mobile_hash         VARCHAR(64)  NULL,
    mobile_last4        VARCHAR(4)   NULL,
    registered_user_id  BIGINT       NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at          DATETIME     NULL,
    UNIQUE KEY uk_event_guest_token (event_id, guest_token),
    KEY idx_event_guest_event (event_id),
    KEY idx_event_guest_mobile (event_id, mobile_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE event_signups
    MODIFY user_id BIGINT NULL,
    ADD COLUMN guest_participant_id BIGINT NULL AFTER user_id;

ALTER TABLE event_signups
    DROP INDEX uk_event_signups_event_user;

ALTER TABLE event_signups
    ADD UNIQUE KEY uk_event_signups_event_user (event_id, user_id),
    ADD UNIQUE KEY uk_event_signups_event_guest (event_id, guest_participant_id);

ALTER TABLE dating_event_identity
    ADD COLUMN row_id BIGINT AUTO_INCREMENT UNIQUE FIRST,
    ADD COLUMN guest_participant_id BIGINT NULL AFTER user_id;

ALTER TABLE dating_event_identity
    DROP PRIMARY KEY,
    MODIFY user_id BIGINT NULL,
    ADD PRIMARY KEY (row_id),
    ADD UNIQUE KEY uk_dating_identity_event_user (event_id, user_id),
    ADD UNIQUE KEY uk_dating_identity_event_guest (event_id, guest_participant_id);

ALTER TABLE dating_event_profile
    ADD COLUMN row_id BIGINT AUTO_INCREMENT UNIQUE FIRST,
    ADD COLUMN guest_participant_id BIGINT NULL AFTER user_id;

ALTER TABLE dating_event_profile
    DROP PRIMARY KEY,
    MODIFY user_id BIGINT NULL,
    ADD PRIMARY KEY (row_id),
    ADD UNIQUE KEY uk_dating_profile_event_user (event_id, user_id),
    ADD UNIQUE KEY uk_dating_profile_event_guest (event_id, guest_participant_id);
