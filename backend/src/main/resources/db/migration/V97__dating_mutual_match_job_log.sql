CREATE TABLE dating_mutual_match_job_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    event_id    VARCHAR(64)  NOT NULL,
    job_type    VARCHAR(64)  NOT NULL,
    executed_at DATETIME     NOT NULL,
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dating_mutual_match_job (event_id, job_type),
    KEY idx_dating_mutual_match_job_executed (executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
