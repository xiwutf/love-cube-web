SET @db = DATABASE();

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

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE user_profiles ADD COLUMN user_id BIGINT NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles' AND COLUMN_NAME = 'user_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE user_profiles MODIFY COLUMN nickname VARCHAR(50) NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles' AND COLUMN_NAME = 'nickname' AND IS_NULLABLE = 'NO'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE user_profiles MODIFY COLUMN avatar VARCHAR(255) NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles' AND COLUMN_NAME = 'avatar' AND IS_NULLABLE = 'NO'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE user_profiles MODIFY COLUMN age INT NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles' AND COLUMN_NAME = 'age' AND IS_NULLABLE = 'NO'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE user_profiles MODIFY COLUMN gender VARCHAR(10) NULL',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles' AND COLUMN_NAME = 'gender' AND IS_NULLABLE = 'NO'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE user_profiles ADD UNIQUE INDEX uk_user_profiles_user_id (user_id)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_profiles' AND INDEX_NAME = 'uk_user_profiles_user_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE user_profiles up
JOIN users u ON up.user_id IS NULL AND up.id = u.userid
SET up.user_id = u.userid
WHERE up.user_id IS NULL;

INSERT INTO user_profiles (user_id, nickname, avatar, age, gender, city, education, last_active_time, create_time, update_time)
SELECT
    u.userid,
    u.username,
    u.profile_photo,
    u.age,
    CASE WHEN u.gender = 1 THEN 'male' WHEN u.gender = 2 THEN 'female' ELSE NULL END,
    u.location,
    NULL,
    NOW(),
    COALESCE(u.created_at, NOW()),
    NOW()
FROM users u
LEFT JOIN user_profiles up ON up.user_id = u.userid
WHERE up.id IS NULL;

INSERT INTO fellowship_profiles (user_id, nickname, avatar, gender, birthday, city, occupation, height, bio, photos_json, completion_rate, verification_status, review_status, created_at, updated_at)
SELECT
    u.userid,
    COALESCE(fp.nickname, u.username),
    COALESCE(fp.avatar_url, u.profile_photo),
    COALESCE(fp.gender, CASE WHEN u.gender = 1 THEN 'male' WHEN u.gender = 2 THEN 'female' ELSE NULL END),
    CASE
      WHEN fp.birth_year IS NOT NULL THEN STR_TO_DATE(CONCAT(fp.birth_year, '-01-01'), '%Y-%m-%d')
      WHEN u.birth_date IS NOT NULL THEN DATE(u.birth_date)
      ELSE NULL
    END,
    COALESCE(fp.city, u.location),
    COALESCE(fp.occupation, u.occupation),
    COALESCE(fp.height, u.height),
    COALESCE(fp.bio, u.bio),
    COALESCE(u.photos, '[]'),
    0,
    'none',
    LOWER(COALESCE(fp.review_status, 'pending')),
    NOW(),
    NOW()
FROM users u
LEFT JOIN fellowship_profile fp ON fp.user_id = u.userid
LEFT JOIN fellowship_profiles fps ON fps.user_id = u.userid
WHERE fps.id IS NULL;

UPDATE fellowship_profiles fps
LEFT JOIN fellowship_profile fp ON fp.user_id = fps.user_id
SET fps.nickname = COALESCE(fps.nickname, fp.nickname),
    fps.avatar = COALESCE(fps.avatar, fp.avatar_url),
    fps.gender = COALESCE(fps.gender, fp.gender),
    fps.city = COALESCE(fps.city, fp.city),
    fps.occupation = COALESCE(fps.occupation, fp.occupation),
    fps.height = COALESCE(fps.height, fp.height),
    fps.bio = COALESCE(fps.bio, fp.bio),
    fps.updated_at = NOW()
WHERE fp.user_id IS NOT NULL;
