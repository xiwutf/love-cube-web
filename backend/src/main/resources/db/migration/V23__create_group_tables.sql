CREATE TABLE IF NOT EXISTS platform_groups (
  id VARCHAR(64) NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  cover_url VARCHAR(512),
  category VARCHAR(50),
  status VARCHAR(20) NOT NULL DEFAULT 'active',
  join_type VARCHAR(20) NOT NULL DEFAULT 'approval',
  member_count INT NOT NULL DEFAULT 0,
  pinned TINYINT(1) NOT NULL DEFAULT 0,
  created_by BIGINT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  group_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'member',
  joined_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_user (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_posts (
  id VARCHAR(64) NOT NULL,
  group_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL DEFAULT 'post',
  content TEXT NOT NULL,
  like_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_group_posts_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_join_requests (
  id BIGINT NOT NULL AUTO_INCREMENT,
  group_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'pending',
  message VARCHAR(255),
  requested_at DATETIME NOT NULL,
  handled_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_gjr_group_id (group_id),
  KEY idx_gjr_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
