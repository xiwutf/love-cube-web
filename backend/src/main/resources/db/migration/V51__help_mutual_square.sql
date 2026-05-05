CREATE TABLE help_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    help_type VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    region VARCHAR(100) NULL,
    contact_type VARCHAR(32) NULL,
    contact_value VARCHAR(200) NULL,
    contact_public TINYINT(1) NOT NULL DEFAULT 0,
    deadline DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    helper_user_id BIGINT NULL,
    resolved_at DATETIME NULL,
    resolved_note VARCHAR(500) NULL,
    reply_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_help_request_status_created (status, created_at),
    INDEX idx_help_request_user_created (user_id, created_at),
    INDEX idx_help_request_type (help_type)
);

CREATE TABLE help_reply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    contact_willing TINYINT(1) NOT NULL DEFAULT 0,
    contact_type VARCHAR(32) NULL,
    contact_value VARCHAR(200) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    is_helper TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_help_reply_request_user UNIQUE (request_id, user_id),
    INDEX idx_help_reply_request (request_id),
    INDEX idx_help_reply_user (user_id)
);

CREATE TABLE user_help_stats (
    user_id BIGINT PRIMARY KEY,
    help_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    accepted_count INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
