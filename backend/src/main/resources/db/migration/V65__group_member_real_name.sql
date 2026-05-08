-- 成员在团体内展示的「真实姓名」，不出现在公共个人资料接口中。
ALTER TABLE group_members ADD COLUMN member_real_name VARCHAR(64) NULL COMMENT '团体内成员展示姓名';
ALTER TABLE platform_group_member ADD COLUMN member_real_name VARCHAR(64) NULL COMMENT '团体内成员展示姓名';
ALTER TABLE group_join_requests ADD COLUMN member_real_name VARCHAR(64) NULL COMMENT '加入申请时登记的在团体内展示姓名';
