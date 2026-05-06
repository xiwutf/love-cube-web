CREATE TABLE user_invite_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    invite_code VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_invite_code_user_id (user_id),
    UNIQUE KEY uk_user_invite_code_invite_code (invite_code),
    INDEX idx_user_invite_code_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_invite_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invite_code VARCHAR(32) NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    invited_user_id BIGINT NOT NULL,
    register_ip VARCHAR(64) NULL,
    register_user_agent VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_invite_relation_invited_user_id (invited_user_id),
    INDEX idx_user_invite_relation_inviter_user_id (inviter_user_id),
    INDEX idx_user_invite_relation_invite_code (invite_code),
    INDEX idx_user_invite_relation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO user_invite_code (user_id, invite_code, status, created_at, updated_at)
SELECT
    u.userid,
    UPPER(COALESCE(NULLIF(TRIM(u.invite_code), ''), CONCAT('LC', u.userid, SUBSTRING(MD5(CONCAT(u.userid, ':lovecube')), 1, 4)))),
    COALESCE(NULLIF(TRIM(u.invite_code_status), ''), 'ENABLED'),
    COALESCE(u.created_at, CURRENT_TIMESTAMP),
    COALESCE(u.updated_at, CURRENT_TIMESTAMP)
FROM users u;

INSERT INTO user_invite_relation (invite_code, inviter_user_id, invited_user_id, register_ip, register_user_agent, status, created_at)
SELECT
    UPPER(TRIM(r.invite_code)),
    r.inviter_user_id,
    r.invitee_user_id,
    r.register_ip,
    r.register_user_agent,
    COALESCE(NULLIF(TRIM(r.status), ''), 'SUCCESS'),
    COALESCE(r.created_at, CURRENT_TIMESTAMP)
FROM invite_record r
JOIN (
    SELECT invitee_user_id, MIN(id) AS id
    FROM invite_record
    WHERE inviter_user_id IS NOT NULL
      AND invitee_user_id IS NOT NULL
      AND inviter_user_id <> invitee_user_id
      AND invite_code IS NOT NULL
      AND TRIM(invite_code) <> ''
    GROUP BY invitee_user_id
) picked ON picked.id = r.id
WHERE r.inviter_user_id IS NOT NULL
  AND r.invitee_user_id IS NOT NULL
  AND r.inviter_user_id <> r.invitee_user_id
  AND r.invite_code IS NOT NULL
  AND TRIM(r.invite_code) <> '';
