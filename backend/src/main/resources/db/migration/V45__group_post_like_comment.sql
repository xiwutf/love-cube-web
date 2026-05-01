-- 给 platform_group_post 加点赞数、评论数
ALTER TABLE platform_group_post
    ADD COLUMN like_count    INT NOT NULL DEFAULT 0 AFTER image_urls,
    ADD COLUMN comment_count INT NOT NULL DEFAULT 0 AFTER like_count;

-- 给 platform_group 加动态数、最近活跃时间
ALTER TABLE platform_group
    ADD COLUMN post_count      INT      NOT NULL DEFAULT 0 AFTER member_count,
    ADD COLUMN last_active_at  DATETIME NULL     AFTER post_count;

-- 团体动态点赞表
CREATE TABLE platform_group_post_like (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    post_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_user (post_id, user_id),
    KEY idx_post_id  (post_id),
    KEY idx_user_id  (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 团体动态评论表
CREATE TABLE platform_group_post_comment (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    post_id    BIGINT       NOT NULL,
    group_id   BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    content    VARCHAR(500) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'published',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_post_created  (post_id, created_at),
    KEY idx_group_created (group_id, created_at),
    KEY idx_user_id       (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
