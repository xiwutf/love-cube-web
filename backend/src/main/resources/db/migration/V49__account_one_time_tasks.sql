CREATE TABLE account_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    check_type VARCHAR(40) NOT NULL,
    reward_exp INT NOT NULL,
    sort_no INT DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE user_account_task_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_code VARCHAR(50) NOT NULL,
    completed TINYINT NOT NULL DEFAULT 0,
    claimed TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_account_task (user_id, task_code)
);

INSERT INTO account_tasks (code, name, check_type, reward_exp, sort_no, enabled) VALUES
('ACC_AVATAR', '上传头像', 'HAS_PROFILE_PHOTO', 80, 1, 1),
('ACC_PHOTO', '上传至少一张照片', 'HAS_GALLERY_PHOTO', 100, 2, 1),
('ACC_BIO', '完善个人简介（8 字以上）', 'HAS_BIO_MIN_LEN', 60, 3, 1),
('ACC_JOIN_GROUP', '加入一个平台团体', 'IN_APPROVED_PLATFORM_GROUP', 120, 4, 1),
('ACC_FIRST_POST', '首次发布内容', 'HAS_POSTED_CONTENT', 150, 5, 1),
('ACC_BIND_PHONE', '绑定手机号', 'HAS_PHONE_BOUND', 90, 6, 1);
