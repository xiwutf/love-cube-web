CREATE TABLE local_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    location VARCHAR(100),
    event_time DATETIME NULL,
    summary VARCHAR(500),
    cover_url VARCHAR(500),
    heat INT NOT NULL DEFAULT 0,
    interest_count INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'draft',
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_local_resource_status (status),
    INDEX idx_local_resource_type (type),
    INDEX idx_local_resource_updated_at (updated_at),
    INDEX idx_local_resource_heat (heat)
);
