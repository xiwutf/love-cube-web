CREATE TABLE IF NOT EXISTS invite_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invite_code VARCHAR(32) NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    invitee_user_id BIGINT NULL,
    invitee_username VARCHAR(128) NULL,
    register_ip VARCHAR(64) NULL,
    register_user_agent VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_invite_record_invite_code (invite_code),
    INDEX idx_invite_record_inviter_status (inviter_user_id, status),
    INDEX idx_invite_record_invitee (invitee_user_id)
);
