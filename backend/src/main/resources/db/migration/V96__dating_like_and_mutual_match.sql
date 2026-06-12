CREATE TABLE dating_like (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    event_id                 VARCHAR(64)  NOT NULL,
    participant_type         VARCHAR(16)  NOT NULL,
    participant_id           BIGINT       NOT NULL,
    target_participant_type  VARCHAR(16)  NOT NULL,
    target_participant_id    BIGINT       NOT NULL,
    created_at               DATETIME     NOT NULL,
    updated_at               DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dating_like_pair (
        event_id,
        participant_type,
        participant_id,
        target_participant_type,
        target_participant_id
    ),
    KEY idx_dating_like_event_participant (event_id, participant_type, participant_id, created_at),
    KEY idx_dating_like_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dating_mutual_match (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    event_id                 VARCHAR(64)  NOT NULL,
    participant_a_type       VARCHAR(16)  NOT NULL,
    participant_a_id         BIGINT       NOT NULL,
    participant_b_type       VARCHAR(16)  NOT NULL,
    participant_b_id         BIGINT       NOT NULL,
    matched_at               DATETIME     NOT NULL,
    created_at               DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dating_mutual_match_pair (
        event_id,
        participant_a_type,
        participant_a_id,
        participant_b_type,
        participant_b_id
    ),
    KEY idx_dating_mutual_match_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
