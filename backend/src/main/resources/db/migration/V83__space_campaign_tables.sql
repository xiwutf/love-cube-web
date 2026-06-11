CREATE TABLE space_campaign (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    group_id        BIGINT       NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT         NULL,
    start_date      DATE         NOT NULL,
    duration_days   INT          NOT NULL DEFAULT 7,
    status          VARCHAR(32)  NOT NULL DEFAULT 'active',
    created_by      BIGINT       NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_space_campaign_group (group_id),
    KEY idx_space_campaign_group_status (group_id, status),
    KEY idx_space_campaign_start (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE space_campaign_day (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    campaign_id      BIGINT       NOT NULL,
    day_number       INT          NOT NULL,
    task_title       VARCHAR(200) NOT NULL,
    task_description TEXT         NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_scd_campaign_day (campaign_id, day_number),
    KEY idx_scd_campaign (campaign_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE space_campaign_progress (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    campaign_id  BIGINT   NOT NULL,
    user_id      BIGINT   NOT NULL,
    day_number   INT      NOT NULL,
    completed_at DATETIME NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_scp_campaign_user_day (campaign_id, user_id, day_number),
    KEY idx_scp_campaign (campaign_id),
    KEY idx_scp_user (user_id),
    KEY idx_scp_campaign_day (campaign_id, day_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
