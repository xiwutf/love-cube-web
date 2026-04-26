CREATE TABLE IF NOT EXISTS verification_requests (
    id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    id_number VARCHAR(64) NOT NULL,
    note VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    reject_reason VARCHAR(500) NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64) NULL,
    id_number VARCHAR(64) NULL,
    id_front_url VARCHAR(512) NULL,
    id_back_url VARCHAR(512) NULL,
    selfie_url VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    reject_reason VARCHAR(500) NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_verifications_user (user_id),
    INDEX idx_user_verifications_status (status)
);

INSERT INTO user_verifications (
    user_id,
    real_name,
    id_number,
    status,
    reject_reason,
    submitted_at,
    reviewed_at,
    created_at,
    updated_at
)
SELECT
    vr.user_id,
    vr.real_name,
    vr.id_number,
    vr.status,
    vr.reject_reason,
    vr.submitted_at,
    vr.reviewed_at,
    COALESCE(vr.created_at, NOW()),
    NOW()
FROM verification_requests vr
LEFT JOIN user_verifications uv ON uv.user_id = vr.user_id AND uv.id_number = vr.id_number
WHERE uv.id IS NULL;
