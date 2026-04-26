CREATE TABLE IF NOT EXISTS users (
    userid BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    phone_number VARCHAR(255) NULL,
    password_hash VARCHAR(255) NULL,
    openid VARCHAR(255) NOT NULL,
    profile_photo VARCHAR(255) NULL,
    bio VARCHAR(500) NULL,
    age INT NULL,
    gender INT NULL,
    location VARCHAR(255) NULL,
    occupation VARCHAR(255) NULL,
    height INT NULL,
    birth_date DATETIME NULL,
    photos TEXT NULL,
    vip_status VARCHAR(64) NULL,
    vip_expires_at DATETIME NULL,
    invite_code VARCHAR(32) NULL,
    invited_by_user_id BIGINT NULL,
    register_ip VARCHAR(64) NULL,
    register_user_agent VARCHAR(500) NULL,
    user_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    invite_code_status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    UNIQUE KEY uk_users_openid (openid)
);

CREATE TABLE IF NOT EXISTS chatmessages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    timestamp BIGINT NOT NULL,
    is_read BIT(1) NOT NULL DEFAULT b'0'
);

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    nickname VARCHAR(50) NULL,
    avatar VARCHAR(255) NULL,
    age INT NULL,
    gender VARCHAR(10) NULL,
    city VARCHAR(50) NULL,
    province VARCHAR(50) NULL,
    tag VARCHAR(100) NULL,
    has_house BOOLEAN NULL,
    has_car BOOLEAN NULL,
    annual_income INT NULL,
    education VARCHAR(50) NULL,
    has_overseas_experience BOOLEAN NULL,
    is_single BOOLEAN NULL,
    last_active_time DATETIME NULL,
    is_newcomer BOOLEAN DEFAULT TRUE,
    create_time DATETIME NULL,
    update_time DATETIME NULL
);

CREATE TABLE IF NOT EXISTS user_tags (
    user_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, tag)
);

CREATE TABLE IF NOT EXISTS fellowship_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nickname VARCHAR(64) NULL,
    avatar VARCHAR(255) NULL,
    gender VARCHAR(16) NULL,
    birthday DATE NULL,
    city VARCHAR(64) NULL,
    occupation VARCHAR(64) NULL,
    height INT NULL,
    bio VARCHAR(500) NULL,
    photos_json TEXT NULL,
    completion_rate INT NOT NULL DEFAULT 0,
    verification_status VARCHAR(32) NOT NULL DEFAULT 'none',
    verification_note VARCHAR(500) NULL,
    review_status VARCHAR(32) NOT NULL DEFAULT 'pending',
    review_note VARCHAR(500) NULL,
    reported_count INT NOT NULL DEFAULT 0,
    last_active_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fellowship_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nickname VARCHAR(64) NULL,
    gender VARCHAR(16) NULL,
    birth_year INT NULL,
    age INT NULL,
    city VARCHAR(64) NULL,
    occupation VARCHAR(64) NULL,
    education VARCHAR(64) NULL,
    height INT NULL,
    bio VARCHAR(500) NULL,
    intention VARCHAR(500) NULL,
    avatar_url VARCHAR(255) NULL,
    tags TEXT NULL,
    identity_role VARCHAR(32) NOT NULL DEFAULT 'self',
    guardian_role VARCHAR(32) NULL,
    child_gender VARCHAR(16) NULL,
    child_age INT NULL,
    child_height INT NULL,
    child_education VARCHAR(64) NULL,
    child_job VARCHAR(64) NULL,
    child_city VARCHAR(64) NULL,
    child_house_car_status VARCHAR(128) NULL,
    child_marriage_intention VARCHAR(500) NULL,
    child_partner_requirements TEXT NULL,
    guardian_contact_visible TINYINT(1) NOT NULL DEFAULT 1,
    profile_status VARCHAR(16) NOT NULL DEFAULT 'INCOMPLETE',
    review_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    photo_url VARCHAR(512) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_photos_user (user_id)
);

CREATE TABLE IF NOT EXISTS verification_requests (
    id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    id_number VARCHAR(64) NOT NULL,
    note VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    reject_reason VARCHAR(500) NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64) NULL,
    id_number VARCHAR(64) NULL,
    id_front_url VARCHAR(512) NULL,
    id_back_url VARCHAR(512) NULL,
    selfie_url VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    reject_reason VARCHAR(500) NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_verifications_user (user_id),
    INDEX idx_user_verifications_status (status)
);

CREATE TABLE IF NOT EXISTS reports (
    id VARCHAR(64) PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_user_id BIGINT NULL,
    report_type VARCHAR(64) NOT NULL,
    content VARCHAR(1000) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    note VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
