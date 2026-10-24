BEGIN;

-- Migration 2026-10-24_2: Migrate all TIMESTAMP columns to TIMESTAMPTZ (TIMESTAMP WITH TIME ZONE)
-- Anchors all existing historical data to Europe/Berlin timezone with Daylight Saving Time (DST) awareness.

-- 1. session: Drop dependent generated columns, alter timestamps, re-add generated columns
ALTER TABLE session DROP COLUMN IF EXISTS duration;
ALTER TABLE session DROP COLUMN IF EXISTS net_duration;

ALTER TABLE session ALTER COLUMN starttime TYPE TIMESTAMPTZ USING starttime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE session ALTER COLUMN endtime TYPE TIMESTAMPTZ USING endtime AT TIME ZONE 'Europe/Berlin';

ALTER TABLE session ADD COLUMN duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED;
ALTER TABLE session ADD COLUMN net_duration INTERVAL GENERATED ALWAYS AS (endtime - starttime - break_duration) STORED;

-- 2. calendar_event: Drop duration, alter timestamps, re-add duration
ALTER TABLE calendar_event DROP COLUMN IF EXISTS duration;

ALTER TABLE calendar_event ALTER COLUMN starttime TYPE TIMESTAMPTZ USING starttime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE calendar_event ALTER COLUMN endtime TYPE TIMESTAMPTZ USING endtime AT TIME ZONE 'Europe/Berlin';

ALTER TABLE calendar_event ADD COLUMN duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED;

-- 3. sleeptime: Drop sleeptime, alter timestamps, re-add sleeptime
ALTER TABLE sleeptime DROP COLUMN IF EXISTS sleeptime;

ALTER TABLE sleeptime ALTER COLUMN gotobed TYPE TIMESTAMPTZ USING gotobed AT TIME ZONE 'Europe/Berlin';
ALTER TABLE sleeptime ALTER COLUMN standup TYPE TIMESTAMPTZ USING standup AT TIME ZONE 'Europe/Berlin';

ALTER TABLE sleeptime ADD COLUMN sleeptime INTERVAL GENERATED ALWAYS AS (standup - gotobed) STORED;

-- 4. train_trip: Drop duration, alter timestamps, re-add duration
ALTER TABLE train_trip DROP COLUMN IF EXISTS duration;

ALTER TABLE train_trip ALTER COLUMN starttime TYPE TIMESTAMPTZ USING starttime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE train_trip ALTER COLUMN endtime TYPE TIMESTAMPTZ USING endtime AT TIME ZONE 'Europe/Berlin';

ALTER TABLE train_trip ADD COLUMN duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED;

-- 5. train_trip2: Drop duration, alter timestamps, re-add duration
ALTER TABLE train_trip2 DROP COLUMN IF EXISTS duration;

ALTER TABLE train_trip2 ALTER COLUMN starttime TYPE TIMESTAMPTZ USING starttime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE train_trip2 ALTER COLUMN endtime TYPE TIMESTAMPTZ USING endtime AT TIME ZONE 'Europe/Berlin';

ALTER TABLE train_trip2 ADD COLUMN duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED;

-- 6. activity: Drop duration, alter timestamps, re-add duration
ALTER TABLE activity DROP COLUMN IF EXISTS duration;

ALTER TABLE activity ALTER COLUMN starttime TYPE TIMESTAMPTZ USING starttime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE activity ALTER COLUMN endtime TYPE TIMESTAMPTZ USING endtime AT TIME ZONE 'Europe/Berlin';

ALTER TABLE activity ADD COLUMN duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED;

-- 7. habit_entry: alter last_done
ALTER TABLE habit_entry ALTER COLUMN last_done TYPE TIMESTAMPTZ USING last_done AT TIME ZONE 'Europe/Berlin';

-- 8. car_trip: Drop duration, alter timestamps, re-add duration
ALTER TABLE car_trip DROP COLUMN IF EXISTS duration;

ALTER TABLE car_trip ALTER COLUMN starttime TYPE TIMESTAMPTZ USING starttime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE car_trip ALTER COLUMN endtime TYPE TIMESTAMPTZ USING endtime AT TIME ZONE 'Europe/Berlin';

ALTER TABLE car_trip ADD COLUMN duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED;

-- 9. learning_item: alter last_updated
ALTER TABLE learning_item ALTER COLUMN last_updated TYPE TIMESTAMPTZ USING last_updated AT TIME ZONE 'Europe/Berlin';

-- 10. course: alter exam_date, created_at, last_updated
ALTER TABLE course ALTER COLUMN exam_date TYPE TIMESTAMPTZ USING exam_date AT TIME ZONE 'Europe/Berlin';
ALTER TABLE course ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Berlin';
ALTER TABLE course ALTER COLUMN last_updated TYPE TIMESTAMPTZ USING last_updated AT TIME ZONE 'Europe/Berlin';

-- 11. lecture_note: alter created_at, last_updated
ALTER TABLE lecture_note ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Berlin';
ALTER TABLE lecture_note ALTER COLUMN last_updated TYPE TIMESTAMPTZ USING last_updated AT TIME ZONE 'Europe/Berlin';

-- 12. appointment: Drop travel_duration, duration, complete_duration, alter timestamps, re-add generated columns
ALTER TABLE appointment DROP COLUMN IF EXISTS travel_duration;
ALTER TABLE appointment DROP COLUMN IF EXISTS duration;
ALTER TABLE appointment DROP COLUMN IF EXISTS complete_duration;

ALTER TABLE appointment ALTER COLUMN departure TYPE TIMESTAMPTZ USING departure AT TIME ZONE 'Europe/Berlin';
ALTER TABLE appointment ALTER COLUMN arrival TYPE TIMESTAMPTZ USING arrival AT TIME ZONE 'Europe/Berlin';
ALTER TABLE appointment ALTER COLUMN starttime TYPE TIMESTAMPTZ USING starttime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE appointment ALTER COLUMN endtime TYPE TIMESTAMPTZ USING endtime AT TIME ZONE 'Europe/Berlin';
ALTER TABLE appointment ALTER COLUMN arrival_home TYPE TIMESTAMPTZ USING arrival_home AT TIME ZONE 'Europe/Berlin';

ALTER TABLE appointment ADD COLUMN travel_duration INTERVAL GENERATED ALWAYS AS (arrival - departure) STORED;
ALTER TABLE appointment ADD COLUMN duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED;
ALTER TABLE appointment ADD COLUMN complete_duration INTERVAL GENERATED ALWAYS AS (arrival_home - departure) STORED;

-- 13. station: alter created_at
ALTER TABLE station ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Berlin';

-- 14. charging_point: alter created_at
ALTER TABLE charging_point ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Berlin';

-- 15. refueling: alter fueling_date, created_at
ALTER TABLE refueling ALTER COLUMN fueling_date TYPE TIMESTAMPTZ USING fueling_date AT TIME ZONE 'Europe/Berlin';
ALTER TABLE refueling ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Berlin';

-- 16. car_expense: alter created_at
ALTER TABLE car_expense ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'Europe/Berlin';

-- 17. Update check_sleep_session_duration function variables to TIMESTAMPTZ
CREATE OR REPLACE FUNCTION check_sleep_session_duration()
RETURNS TRIGGER AS $$
DECLARE
    v_starttime TIMESTAMPTZ;
    v_endtime TIMESTAMPTZ;
    v_break INTERVAL;
    v_total INTERVAL;
    v_sum INTERVAL;
BEGIN
    SELECT starttime, endtime, break_duration
      INTO v_starttime, v_endtime, v_break
      FROM session
     WHERE id = NEW.id;

    IF v_starttime IS NOT NULL AND v_endtime IS NOT NULL THEN
        v_total := v_endtime - v_starttime;
        v_sum := COALESCE(NEW.deep_sleep_duration, '0 seconds'::interval)
               + COALESCE(NEW.rem_sleep_duration, '0 seconds'::interval)
               + COALESCE(NEW.light_sleep_duration, '0 seconds'::interval)
               + COALESCE(v_break, '0 seconds'::interval);

        IF v_sum <> v_total THEN
            RAISE EXCEPTION 'Sleep stages sum (%) does not equal total session duration (%) for sleep_session id %',
                v_sum, v_total, NEW.id;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMIT;
