BEGIN;

-- Migration 2026-10-24_1: Convert session sleep duration trigger to deferrable constraint trigger
-- This resolves cross-table trigger violations during joined inheritance Hibernate update flushes,
-- deferring check_session_sleep_duration() on session to transaction COMMIT time.

DROP TRIGGER IF EXISTS trg_check_session_sleep_duration ON session;
CREATE CONSTRAINT TRIGGER trg_check_session_sleep_duration
AFTER UPDATE ON session
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION check_session_sleep_duration();

COMMIT;
