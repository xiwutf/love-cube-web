-- Add marker column to dynamics for separating dedup key from display text
ALTER TABLE dynamics ADD COLUMN marker VARCHAR(255) DEFAULT NULL;
ALTER TABLE dynamics ADD INDEX idx_dynamics_marker_scene (user_id, marker, scene_type);

-- Unique constraint on user_achievement(user_id, achievement_code)
-- Remove duplicate rows first (keep lowest id per pair)
DELETE ua1 FROM user_achievement ua1
INNER JOIN user_achievement ua2
  ON ua1.user_id = ua2.user_id
 AND ua1.achievement_code = ua2.achievement_code
 AND ua1.id > ua2.id;

ALTER TABLE user_achievement
  ADD UNIQUE KEY uk_user_achievement_code (user_id, achievement_code);
