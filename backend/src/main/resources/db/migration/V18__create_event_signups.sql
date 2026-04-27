CREATE TABLE event_signups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_event_signups_event_user UNIQUE (event_id, user_id),
    INDEX idx_event_signups_event_id (event_id),
    INDEX idx_event_signups_user_id (user_id)
);
