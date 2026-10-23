BEGIN;

DO $$ BEGIN
    CREATE TYPE academic_activity_type AS ENUM (
        'LECTURE',
        'PREPARATION',
        'LEARNING',
        'HOMEWORK',
        'LAB',
        'PROJECT',
        'PRESENTATION'
    );
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

ALTER TABLE lecture_session
    ADD COLUMN IF NOT EXISTS activity_type academic_activity_type NOT NULL DEFAULT 'LECTURE';

CREATE INDEX IF NOT EXISTS idx_lecture_session_activity_type ON lecture_session(activity_type);

COMMIT;
