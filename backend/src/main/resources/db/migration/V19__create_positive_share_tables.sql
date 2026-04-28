CREATE TABLE positive_share (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(32) NOT NULL,
    is_anonymous TINYINT(1) NOT NULL DEFAULT 0,
    public_visible TINYINT(1) NOT NULL DEFAULT 1,
    status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
    encourage_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_positive_share_status_public_created (status, public_visible, created_at),
    INDEX idx_positive_share_user_created (user_id, created_at),
    INDEX idx_positive_share_category (category)
);

CREATE TABLE positive_share_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    share_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_positive_share_like_share_user UNIQUE (share_id, user_id),
    INDEX idx_positive_share_like_share (share_id),
    INDEX idx_positive_share_like_user (user_id)
);

CREATE TABLE positive_share_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    share_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_positive_share_comment_share_created (share_id, created_at),
    INDEX idx_positive_share_comment_user (user_id)
);
