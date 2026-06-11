ALTER TABLE growth_campaign_delivery
    ADD COLUMN clicked_count INT NOT NULL DEFAULT 0 AFTER clicked_at,
    ADD COLUMN last_clicked_at DATETIME NULL AFTER clicked_count;

CREATE TABLE growth_campaign_click_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    campaign_id BIGINT NOT NULL,
    delivery_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    action_url VARCHAR(512) NULL,
    clicked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent VARCHAR(500) NULL,
    ip_address VARCHAR(64) NULL,
    PRIMARY KEY (id),
    KEY idx_gcce_campaign (campaign_id),
    KEY idx_gcce_delivery (delivery_id),
    KEY idx_gcce_user (user_id),
    KEY idx_gcce_clicked_at (clicked_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
