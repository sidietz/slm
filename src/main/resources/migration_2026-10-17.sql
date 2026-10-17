-- Migration: 2026-10-17 Introduce Employer table and update working_session
BEGIN;

CREATE TABLE IF NOT EXISTS employer (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

ALTER TABLE working_session DROP CONSTRAINT IF EXISTS working_session_contractor_id_fkey;
DROP INDEX IF EXISTS idx_working_session_contractor;

ALTER TABLE working_session RENAME COLUMN contractor_id TO employer_id;

ALTER TABLE working_session 
    ADD CONSTRAINT working_session_employer_id_fkey 
    FOREIGN KEY (employer_id) REFERENCES employer(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_working_session_employer ON working_session(employer_id);

COMMIT;
