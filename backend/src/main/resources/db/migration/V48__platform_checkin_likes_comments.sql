-- 打卡点赞（同一用户对同一打卡仅一条）
CREATE TABLE platform_checkin_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    checkin_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_checkin_user (checkin_id, user_id),
    KEY idx_checkin_id (checkin_id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 打卡评论（软删除：deleted_at）
CREATE TABLE platform_checkin_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    checkin_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(200) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_checkin_id (checkin_id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
