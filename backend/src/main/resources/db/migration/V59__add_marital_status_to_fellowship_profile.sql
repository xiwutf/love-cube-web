ALTER TABLE fellowship_profile
    ADD COLUMN marital_status VARCHAR(32) NULL COMMENT '婚姻状况: 单身/已婚/离异' AFTER intention;

