CREATE TABLE dating_connection (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    event_id                 VARCHAR(64)  NOT NULL,
    participant_type         VARCHAR(16)  NOT NULL,
    participant_id           BIGINT       NOT NULL,
    target_participant_type  VARCHAR(16)  NOT NULL,
    target_participant_id    BIGINT       NOT NULL,
    created_at               DATETIME     NOT NULL,
    updated_at               DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dating_connection_pair (
        event_id,
        participant_type,
        participant_id,
        target_participant_type,
        target_participant_id
    ),
    KEY idx_dating_connection_event_participant (event_id, participant_type, participant_id, created_at),
    KEY idx_dating_connection_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
