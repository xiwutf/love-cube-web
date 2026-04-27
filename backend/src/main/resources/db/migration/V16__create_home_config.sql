CREATE TABLE IF NOT EXISTS home_configs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(100) NOT NULL,
  config_value LONGTEXT NOT NULL,
  config_group VARCHAR(50) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_home_config_group_key (config_group, config_key),
  KEY idx_home_config_group_sort (config_group, sort_order),
  KEY idx_home_config_enabled (enabled)
);
