-- RBAC: 用户角色表
CREATE TABLE admin_user_role (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    role_code   VARCHAR(64) NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_role (user_id, role_code),
    INDEX idx_aur_user_id (user_id),
    INDEX idx_aur_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RBAC: 角色权限表
CREATE TABLE admin_role_permission (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_code       VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    UNIQUE KEY uq_role_permission (role_code, permission_code),
    INDEX idx_arp_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 团体管理员关系表
CREATE TABLE platform_group_admin (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    group_id    VARCHAR(64) NOT NULL,
    user_id     BIGINT NOT NULL,
    role        VARCHAR(32) NOT NULL DEFAULT 'OWNER',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_pga_group_user (group_id, user_id),
    INDEX idx_pga_user_id (user_id),
    INDEX idx_pga_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SUPER_ADMIN 权限（全站最高权限）
INSERT INTO admin_role_permission (role_code, permission_code) VALUES
('SUPER_ADMIN', 'system.manage'),
('SUPER_ADMIN', 'user.manage'),
('SUPER_ADMIN', 'content.manage'),
('SUPER_ADMIN', 'content.announcement.manage'),
('SUPER_ADMIN', 'content.article.manage'),
('SUPER_ADMIN', 'content.event.manage'),
('SUPER_ADMIN', 'review.manage'),
('SUPER_ADMIN', 'group.manage.all'),
('SUPER_ADMIN', 'group.manage.own'),
('SUPER_ADMIN', 'group.member.review'),
('SUPER_ADMIN', 'group.notice.manage'),
('SUPER_ADMIN', 'group.dynamic.manage');

-- CONTENT_ADMIN 权限（内容管理员）
INSERT INTO admin_role_permission (role_code, permission_code) VALUES
('CONTENT_ADMIN', 'content.manage'),
('CONTENT_ADMIN', 'content.announcement.manage'),
('CONTENT_ADMIN', 'content.article.manage'),
('CONTENT_ADMIN', 'content.event.manage'),
('CONTENT_ADMIN', 'review.manage');

-- GROUP_OWNER 权限（团体管理员）
INSERT INTO admin_role_permission (role_code, permission_code) VALUES
('GROUP_OWNER', 'group.manage.own'),
('GROUP_OWNER', 'group.member.review'),
('GROUP_OWNER', 'group.notice.manage'),
('GROUP_OWNER', 'group.dynamic.manage');

-- REVIEWER 权限（审核员）
INSERT INTO admin_role_permission (role_code, permission_code) VALUES
('REVIEWER', 'review.manage');
