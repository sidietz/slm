-- ============================================================================
-- Migration: Generalize All Sessions into Unified Joined Session Hierarchy
-- Adds: session (base), coding_session, working_session
-- Updates: learning_session, gaming_session, reading_session, meditation_session, sport_session
-- Includes: break_duration, gross duration, and net_duration (endtime - starttime - break_duration)
-- Uses: RFC 9562 UUIDv7 primary keys for optimal B-Tree performance
-- ============================================================================

BEGIN;

-- 0. Helper function to generate time-ordered UUIDv7 in PostgreSQL (RFC 9562)
-- Uses native uuidv7() in PostgreSQL 18+ with zero-dependency fallback for earlier versions
CREATE OR REPLACE FUNCTION generate_uuid_v7() RETURNS uuid AS $$
BEGIN
    RETURN uuidv7();
EXCEPTION WHEN undefined_function THEN
    RETURN (
        lpad(to_hex((EXTRACT(EPOCH FROM clock_timestamp()) * 1000)::bigint), 12, '0') ||
        '-7' || substring(replace(gen_random_uuid()::text, '-', '') from 14 for 3) ||
        '-8' || substring(replace(gen_random_uuid()::text, '-', '') from 18 for 3) ||
        '-' || substring(replace(gen_random_uuid()::text, '-', '') from 21 for 12)
    )::uuid;
END;
$$ LANGUAGE plpgsql;

-- 1. Rename existing legacy tables to hold data during migration (guarded to avoid notices)
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'learning_session') THEN
        ALTER TABLE learning_session RENAME TO old_learning_session;
    END IF;
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'gaming_session') THEN
        ALTER TABLE gaming_session RENAME TO old_gaming_session;
    END IF;
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'readingsession') THEN
        ALTER TABLE readingsession RENAME TO old_readingsession;
    ELSIF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'reading_session') THEN
        ALTER TABLE reading_session RENAME TO old_readingsession;
    END IF;
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'meditationsession') THEN
        ALTER TABLE meditationsession RENAME TO old_meditationsession;
    ELSIF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'meditation_session') THEN
        ALTER TABLE meditation_session RENAME TO old_meditationsession;
    END IF;
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'sportsession') THEN
        ALTER TABLE sportsession RENAME TO old_sportsession;
    ELSIF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'sport_session') THEN
        ALTER TABLE sport_session RENAME TO old_sportsession;
    END IF;
END $$;

-- 2. Create the unified base session table
CREATE TABLE session (
    id TEXT PRIMARY KEY,
    session_type TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    starttime TIMESTAMP NOT NULL,
    endtime TIMESTAMP,
    break_duration INTERVAL NOT NULL DEFAULT '0 seconds',
    duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED,
    net_duration INTERVAL GENERATED ALWAYS AS (endtime - starttime - break_duration) STORED,
    focus_rating SMALLINT CHECK (focus_rating BETWEEN 1 AND 5),
    comment TEXT,
    outcome_summary TEXT
);

-- Performance indices
CREATE INDEX idx_session_type ON session(session_type);
CREATE INDEX idx_session_starttime ON session(starttime);
CREATE INDEX idx_session_status ON session(status);

-- 3. Create joined child tables
CREATE TABLE learning_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    learning_item_id BIGINT REFERENCES learning_item(id),
    units_completed INTEGER
);
CREATE INDEX idx_learning_session_item ON learning_session(learning_item_id);

CREATE TABLE gaming_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    game_id BIGINT NOT NULL REFERENCES game(id),
    device_id BIGINT REFERENCES device(id),
    game_mode TEXT
);
CREATE INDEX idx_gaming_session_game ON gaming_session(game_id);
CREATE INDEX idx_gaming_session_device ON gaming_session(device_id);

CREATE TABLE coding_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    project TEXT,
    repository TEXT,
    branch TEXT,
    ticket TEXT,
    language TEXT
);
CREATE INDEX idx_coding_session_project ON coding_session(project);

CREATE TABLE working_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    contractor_id BIGINT REFERENCES contractor(id),
    project TEXT,
    billable BOOLEAN DEFAULT TRUE,
    hourly_rate NUMERIC(10, 2),
    task_category TEXT
);
CREATE INDEX idx_working_session_contractor ON working_session(contractor_id);
CREATE INDEX idx_working_session_project ON working_session(project);

CREATE TABLE reading_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    book_id BIGINT REFERENCES book(id),
    start_page_count INTEGER,
    end_page_count INTEGER,
    reading_speed REAL
);
CREATE INDEX idx_reading_session_book ON reading_session(book_id);

CREATE TABLE meditation_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE
);

CREATE TABLE sport_session (
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    type BIGINT REFERENCES sporttype(id)
);
CREATE INDEX idx_sport_session_type ON sport_session(type);

-- 4. Migrate data from old_learning_session if present
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'old_learning_session') THEN
        WITH migrated_learning AS (
            SELECT 
                generate_uuid_v7()::text AS new_id,
                learning_item_id,
                starttime,
                endtime,
                comment
            FROM old_learning_session
        ),
        ins_base AS (
            INSERT INTO session (id, session_type, status, starttime, endtime, break_duration, comment)
            SELECT new_id, 'LEARNING', 'COMPLETED', starttime, endtime, '0 seconds'::interval, comment
            FROM migrated_learning
        )
        INSERT INTO learning_session (id, learning_item_id)
        SELECT new_id, learning_item_id
        FROM migrated_learning;

        DROP TABLE old_learning_session;
    END IF;
END $$;

-- 5. Migrate data from old_gaming_session if present
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'old_gaming_session') THEN
        WITH migrated_gaming AS (
            SELECT 
                generate_uuid_v7()::text AS new_id,
                game_id,
                device_id,
                starttime,
                endtime,
                comment
            FROM old_gaming_session
        ),
        ins_base AS (
            INSERT INTO session (id, session_type, status, starttime, endtime, break_duration, comment)
            SELECT new_id, 'GAMING', 'COMPLETED', starttime, endtime, '0 seconds'::interval, comment
            FROM migrated_gaming
        )
        INSERT INTO gaming_session (id, game_id, device_id)
        SELECT new_id, game_id, device_id
        FROM migrated_gaming;

        DROP TABLE old_gaming_session;
    END IF;
END $$;

-- 6. Migrate data from old_readingsession if present
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'old_readingsession') THEN
        WITH migrated_reading AS (
            SELECT 
                generate_uuid_v7()::text AS new_id,
                book_id,
                start_page_count,
                end_page_count,
                reading_speed,
                starttime,
                endtime
            FROM old_readingsession
        ),
        ins_base AS (
            INSERT INTO session (id, session_type, status, starttime, endtime, break_duration, comment)
            SELECT new_id, 'READING', 'COMPLETED', starttime, endtime, '0 seconds'::interval, NULL
            FROM migrated_reading
        )
        INSERT INTO reading_session (id, book_id, start_page_count, end_page_count, reading_speed)
        SELECT new_id, book_id, start_page_count, end_page_count, reading_speed
        FROM migrated_reading;

        DROP TABLE old_readingsession;
    END IF;
END $$;

-- 7. Migrate data from old_meditationsession if present
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'old_meditationsession') THEN
        WITH migrated_meditation AS (
            SELECT 
                generate_uuid_v7()::text AS new_id,
                starttime,
                endtime
            FROM old_meditationsession
        ),
        ins_base AS (
            INSERT INTO session (id, session_type, status, starttime, endtime, break_duration, comment)
            SELECT new_id, 'MEDITATION', 'COMPLETED', starttime, endtime, '0 seconds'::interval, NULL
            FROM migrated_meditation
        )
        INSERT INTO meditation_session (id)
        SELECT new_id
        FROM migrated_meditation;

        DROP TABLE old_meditationsession;
    END IF;
END $$;

-- 8. Migrate data from old_sportsession if present
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'old_sportsession') THEN
        WITH migrated_sport AS (
            SELECT 
                generate_uuid_v7()::text AS new_id,
                type,
                starttime,
                endtime,
                comment
            FROM old_sportsession
        ),
        ins_base AS (
            INSERT INTO session (id, session_type, status, starttime, endtime, break_duration, comment)
            SELECT new_id, 'SPORT', 'COMPLETED', starttime, endtime, '0 seconds'::interval, comment
            FROM migrated_sport
        )
        INSERT INTO sport_session (id, type)
        SELECT new_id, type
        FROM migrated_sport;

        DROP TABLE old_sportsession;
    END IF;
END $$;

COMMIT;
