-- Migration 2026-10-19_1: Migrate Session Hierarchy and Related Foreign Keys from TEXT to native UUIDv7

-- 1. Drop existing foreign key constraints referencing session(id) and lecture_session(id)
ALTER TABLE IF EXISTS lecture_note DROP CONSTRAINT IF EXISTS lecture_note_lecture_session_id_fkey;
ALTER TABLE IF EXISTS sport_session DROP CONSTRAINT IF EXISTS sport_session_id_fkey;
ALTER TABLE IF EXISTS meditation_session DROP CONSTRAINT IF EXISTS meditation_session_id_fkey;
ALTER TABLE IF EXISTS reading_session DROP CONSTRAINT IF EXISTS reading_session_id_fkey;
ALTER TABLE IF EXISTS learning_session DROP CONSTRAINT IF EXISTS learning_session_id_fkey;
ALTER TABLE IF EXISTS gaming_session DROP CONSTRAINT IF EXISTS gaming_session_id_fkey;
ALTER TABLE IF EXISTS coding_session DROP CONSTRAINT IF EXISTS coding_session_id_fkey;
ALTER TABLE IF EXISTS working_session DROP CONSTRAINT IF EXISTS working_session_id_fkey;
ALTER TABLE IF EXISTS sleep_session DROP CONSTRAINT IF EXISTS sleep_session_id_fkey;
ALTER TABLE IF EXISTS lecture_session DROP CONSTRAINT IF EXISTS lecture_session_id_fkey;

-- 2. Alter root session table primary key to UUID and set default to uuidv7()
ALTER TABLE session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE session ALTER COLUMN id SET DEFAULT uuidv7();

-- 3. Alter all joined subclass tables primary keys to UUID
ALTER TABLE sport_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE meditation_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE reading_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE learning_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE gaming_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE coding_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE working_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE sleep_session ALTER COLUMN id TYPE UUID USING id::uuid;
ALTER TABLE lecture_session ALTER COLUMN id TYPE UUID USING id::uuid;

-- 4. Alter lecture_note.lecture_session_id foreign key column to UUID
ALTER TABLE lecture_note ALTER COLUMN lecture_session_id TYPE UUID USING lecture_session_id::uuid;

-- 5. Re-create foreign key constraints with ON DELETE rules preserved
ALTER TABLE sport_session ADD CONSTRAINT sport_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE meditation_session ADD CONSTRAINT meditation_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE reading_session ADD CONSTRAINT reading_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE learning_session ADD CONSTRAINT learning_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE gaming_session ADD CONSTRAINT gaming_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE coding_session ADD CONSTRAINT coding_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE working_session ADD CONSTRAINT working_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE sleep_session ADD CONSTRAINT sleep_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;
ALTER TABLE lecture_session ADD CONSTRAINT lecture_session_id_fkey FOREIGN KEY (id) REFERENCES session(id) ON DELETE CASCADE;

ALTER TABLE lecture_note ADD CONSTRAINT lecture_note_lecture_session_id_fkey FOREIGN KEY (lecture_session_id) REFERENCES lecture_session(id) ON DELETE SET NULL;
