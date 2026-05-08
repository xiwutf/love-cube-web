CREATE TABLE content_risk_log (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    original_text TEXT        NOT NULL,
    suggested_text TEXT,
    hit_words   VARCHAR(500),
    risk_level  VARCHAR(20)  NOT NULL,
    user_id     BIGINT,
    context     VARCHAR(100),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_risk_level (risk_level),
    INDEX idx_user_id    (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
