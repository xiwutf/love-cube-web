CREATE TABLE operation_post (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(1000),
    content TEXT,
    type VARCHAR(32) NOT NULL COMMENT 'notice/changelog/news/featured/activity',
    scope VARCHAR(32) NOT NULL DEFAULT 'all' COMMENT 'all/platform/fellowship',
    status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT 'draft/published',
    is_top TINYINT(1) NOT NULL DEFAULT 0,
    publish_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
