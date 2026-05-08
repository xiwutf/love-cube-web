-- platform_groups 下的 group_posts 点赞 / 评论（独立于 plat 的 platform_group_post_* 表）。
ALTER TABLE group_posts
    ADD COLUMN comment_count INT NOT NULL DEFAULT 0 AFTER like_count;

CREATE TABLE group_post_like (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_post_like (post_id, user_id),
    KEY idx_group_post_like_post (post_id),
    KEY idx_group_post_like_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_post_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id VARCHAR(64) NOT NULL,
    group_id VARCHAR(128) NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_gpc_post (post_id),
    KEY idx_gpc_group (group_id),
    KEY idx_gpc_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
