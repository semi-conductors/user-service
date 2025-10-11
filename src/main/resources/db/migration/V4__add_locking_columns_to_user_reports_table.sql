ALTER TABLE user_reports ADD COLUMN claimed_by BIGINT;
ALTER TABLE user_reports ADD COLUMN claimed_at TIMESTAMP;
ALTER TABLE user_reports ADD COLUMN lock_expires_at TIMESTAMP;

-- Index for quick locking checks
CREATE INDEX idx_reports_claimed ON user_reports(claimed_by, lock_expires_at);