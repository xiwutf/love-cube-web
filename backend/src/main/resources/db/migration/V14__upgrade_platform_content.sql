-- Add category, cover_url, pinned, recommended, view_count to all three content tables

ALTER TABLE announcements
  ADD COLUMN category    VARCHAR(64)  DEFAULT NULL,
  ADD COLUMN cover_url   VARCHAR(512) DEFAULT NULL,
  ADD COLUMN pinned      TINYINT(1)   DEFAULT 0,
  ADD COLUMN recommended TINYINT(1)   DEFAULT 0,
  ADD COLUMN view_count  INT          DEFAULT 0;

ALTER TABLE articles
  ADD COLUMN category    VARCHAR(64)  DEFAULT NULL,
  ADD COLUMN cover_url   VARCHAR(512) DEFAULT NULL,
  ADD COLUMN pinned      TINYINT(1)   DEFAULT 0,
  ADD COLUMN recommended TINYINT(1)   DEFAULT 0,
  ADD COLUMN view_count  INT          DEFAULT 0;

ALTER TABLE events
  ADD COLUMN category    VARCHAR(64)  DEFAULT NULL,
  ADD COLUMN cover_url   VARCHAR(512) DEFAULT NULL,
  ADD COLUMN pinned      TINYINT(1)   DEFAULT 0,
  ADD COLUMN recommended TINYINT(1)   DEFAULT 0,
  ADD COLUMN view_count  INT          DEFAULT 0;
