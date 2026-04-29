CREATE TABLE IF NOT EXISTS platform_group (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) NOT NULL,
  cover_url VARCHAR(512),
  type VARCHAR(30) NOT NULL DEFAULT 'region',
  region VARCHAR(50),
  description TEXT,
  owner_user_id BIGINT,
  member_count INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'published',
  join_mode VARCHAR(20) NOT NULL DEFAULT 'audit',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_slug (slug),
  KEY idx_status_count (status, member_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_group_member (
  id BIGINT NOT NULL AUTO_INCREMENT,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'member',
  status VARCHAR(20) NOT NULL DEFAULT 'approved',
  apply_reason VARCHAR(255),
  joined_at DATETIME,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_user (group_id, user_id),
  KEY idx_user_id (user_id),
  KEY idx_group_status (group_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_group_post (
  id BIGINT NOT NULL AUTO_INCREMENT,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  image_urls TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'published',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_group_created (group_id, created_at),
  KEY idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_group_notice (
  id BIGINT NOT NULL AUTO_INCREMENT,
  group_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT,
  created_by BIGINT,
  status VARCHAR(20) NOT NULL DEFAULT 'published',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_group_status (group_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO platform_group (name, slug, cover_url, type, region, description, member_count, status, join_mode, created_at, updated_at) VALUES
('北京青年团契', 'beijing-youth',
 'https://images.unsplash.com/photo-1507692049790-de58290a4334?auto=format&fit=crop&w=420&q=80',
 'region', '北京', '我们是一个充满爱与活力的青年团契，欢迎弟兄姊妹加入！', 120, 'published', 'audit', NOW(), NOW()),
('上海教会团体', 'shanghai-church',
 'https://images.unsplash.com/photo-1438032005730-c779502df39b?auto=format&fit=crop&w=420&q=80',
 'church', '上海', '以上海教会为依托，发布教会通知和活动信息。', 256, 'published', 'audit', NOW(), NOW()),
('广州祷告小组', 'guangzhou-pray',
 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=420&q=80',
 'study', '广州', '祷告是我们的力量，一起为彼此祷告吧！', 68, 'published', 'free', NOW(), NOW()),
('读经分享小组', 'reading-share',
 'https://images.unsplash.com/photo-1490730141103-6cac27aaab94?auto=format&fit=crop&w=420&q=80',
 'study', '全国', '一起读经，一起成长，在神的话语中扎根。', 86, 'published', 'free', NOW(), NOW()),
('音乐敬拜团', 'worship-band',
 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=420&q=80',
 'interest', '全国', '用音乐赞美神，服侍教会，愿每首歌都成为祝福。', 45, 'published', 'audit', NOW(), NOW()),
('家庭团契小组', 'family-fellowship',
 'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=420&q=80',
 'family', '深圳', '在主里建立美好的家庭关系，分享生活点滴。', 32, 'published', 'free', NOW(), NOW());
