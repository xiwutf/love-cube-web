ALTER TABLE platform_group
    ADD COLUMN invite_code VARCHAR(16) NULL COMMENT '邀请制团体入团码';

UPDATE platform_group
SET invite_code = CONCAT('G', UPPER(SUBSTRING(MD5(CONCAT('lc-grp:', id)), 1, 7)))
WHERE join_mode = 'invite'
  AND (invite_code IS NULL OR TRIM(invite_code) = '');

CREATE UNIQUE INDEX uk_platform_group_invite_code ON platform_group (invite_code);
