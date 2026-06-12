-- P4-mini：成员活动投稿审核字段（复用 creator_user_id，不新增 created_by）

ALTER TABLE platform_group_activity
    ADD COLUMN reviewed_by BIGINT DEFAULT NULL COMMENT '审核人 user_id',
    ADD COLUMN reviewed_at DATETIME DEFAULT NULL COMMENT '审核时间',
    ADD COLUMN review_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见';

CREATE INDEX idx_pga_group_status_created
    ON platform_group_activity (group_id, status, created_at);
