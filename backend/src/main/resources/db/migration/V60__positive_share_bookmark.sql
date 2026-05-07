CREATE TABLE positive_share_bookmark (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    share_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_positive_share_bookmark_share_user UNIQUE (share_id, user_id),
    INDEX idx_positive_share_bookmark_share (share_id),
    INDEX idx_positive_share_bookmark_user_created (user_id, created_at)
);
