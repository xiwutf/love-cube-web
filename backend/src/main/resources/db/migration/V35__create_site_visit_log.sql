CREATE TABLE site_visit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  visitor_id VARCHAR(64) NOT NULL,
  path VARCHAR(255) NOT NULL,
  referrer VARCHAR(512),
  ip_address VARCHAR(64),
  user_agent VARCHAR(512),
  device_type VARCHAR(32),
  browser VARCHAR(32),
  os VARCHAR(32),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_site_visit_created_at (created_at),
  KEY idx_site_visit_visitor_created (visitor_id, created_at),
  KEY idx_site_visit_path_created (path, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
