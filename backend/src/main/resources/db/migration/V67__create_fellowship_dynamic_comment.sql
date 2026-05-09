CREATE TABLE dynamic_comment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  dynamic_id BIGINT NOT NULL COMMENT '联谊动态ID，对应 dynamics.id',
  user_id BIGINT NOT NULL COMMENT '评论用户ID',
  content VARCHAR(500) NOT NULL COMMENT '评论正文',
  status VARCHAR(20) NOT NULL DEFAULT 'published' COMMENT 'published/deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dynamic_status_created (dynamic_id, status, created_at),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='联谊动态评论';
