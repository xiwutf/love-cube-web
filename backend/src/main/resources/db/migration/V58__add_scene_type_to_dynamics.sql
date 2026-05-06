ALTER TABLE dynamics
    ADD COLUMN scene_type VARCHAR(20) NOT NULL DEFAULT 'FELLOWSHIP' AFTER is_deleted;

UPDATE dynamics
SET scene_type = 'FELLOWSHIP'
WHERE scene_type IS NULL OR scene_type = '';

CREATE INDEX idx_dynamics_scene_deleted_created
    ON dynamics (scene_type, is_deleted, created_at DESC);
