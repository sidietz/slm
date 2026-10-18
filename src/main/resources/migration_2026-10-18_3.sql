-- Migration 2026-10-18_3: SleepSession joined inheritance table and migration from legacy sleeptime
CREATE TABLE IF NOT EXISTS sleep_session(
    id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE,
    is_nap BOOLEAN DEFAULT FALSE,
    deep_sleep_duration INTERVAL,
    rem_sleep_duration INTERVAL,
    awake_count INTEGER DEFAULT 0
);

-- Migrate legacy sleeptime entries if sleeptime table exists
DO $$
DECLARE
    r RECORD;
    v_session_id TEXT;
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'sleeptime') THEN
        FOR r IN SELECT id, gotobed, standup, sleeptime FROM sleeptime ORDER BY gotobed ASC LOOP
            v_session_id := gen_random_uuid()::text;
            
            INSERT INTO session (id, session_type, status, starttime, endtime, break_duration, comment)
            VALUES (v_session_id, 'SLEEP', 'COMPLETED', r.gotobed, r.standup, '0 seconds'::interval, 'Migrated from legacy sleeptime');
            
            INSERT INTO sleep_session (id, is_nap, awake_count)
            VALUES (v_session_id, FALSE, 0);
        END LOOP;
    END IF;
END $$;
