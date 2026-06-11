ALTER TABLE space_campaign
    ADD COLUMN allow_makeup TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许补卡',
    ADD COLUMN makeup_days_limit INT NOT NULL DEFAULT 2 COMMENT '可补最近 N 天';
