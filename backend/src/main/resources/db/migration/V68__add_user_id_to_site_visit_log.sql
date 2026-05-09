ALTER TABLE site_visit_log
  ADD COLUMN user_id BIGINT NULL AFTER visitor_id;

CREATE INDEX idx_site_visit_user_created ON site_visit_log (user_id, created_at);
