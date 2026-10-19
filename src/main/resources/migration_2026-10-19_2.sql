BEGIN;

-- 1. Add light_sleep_duration column to sleep_session
ALTER TABLE sleep_session ADD COLUMN IF NOT EXISTS light_sleep_duration INTERVAL;

-- 2. Backfill existing records so all sleep stages sum to net sleep duration (endtime - starttime - break_duration)
UPDATE sleep_session ss
   SET light_sleep_duration = (s.endtime - s.starttime - COALESCE(s.break_duration, '0 seconds'::interval))
                            - COALESCE(ss.deep_sleep_duration, '0 seconds'::interval)
                            - COALESCE(ss.rem_sleep_duration, '0 seconds'::interval)
  FROM session s
 WHERE ss.id = s.id
   AND ss.light_sleep_duration IS NULL;

-- 3. Create cross-table validation trigger function on sleep_session
CREATE OR REPLACE FUNCTION check_sleep_session_duration()
RETURNS TRIGGER AS $$
DECLARE
    v_starttime TIMESTAMP;
    v_endtime TIMESTAMP;
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

DROP TRIGGER IF EXISTS trg_check_sleep_session_duration ON sleep_session;
CREATE TRIGGER trg_check_sleep_session_duration
BEFORE INSERT OR UPDATE ON sleep_session
FOR EACH ROW
EXECUTE FUNCTION check_sleep_session_duration();

-- 4. Create cross-table validation trigger function on session
CREATE OR REPLACE FUNCTION check_session_sleep_duration()
RETURNS TRIGGER AS $$
DECLARE
    v_deep INTERVAL;
    v_rem INTERVAL;
    v_light INTERVAL;
    v_total INTERVAL;
    v_sum INTERVAL;
BEGIN
    IF NEW.session_type = 'SLEEP' AND NEW.starttime IS NOT NULL AND NEW.endtime IS NOT NULL THEN
        SELECT deep_sleep_duration, rem_sleep_duration, light_sleep_duration
          INTO v_deep, v_rem, v_light
          FROM sleep_session
         WHERE id = NEW.id;

        IF FOUND THEN
            v_total := NEW.endtime - NEW.starttime;
            v_sum := COALESCE(v_deep, '0 seconds'::interval)
                   + COALESCE(v_rem, '0 seconds'::interval)
                   + COALESCE(v_light, '0 seconds'::interval)
                   + COALESCE(NEW.break_duration, '0 seconds'::interval);

            IF v_sum <> v_total THEN
                RAISE EXCEPTION 'Sleep stages sum (%) does not equal total session duration (%) for session id %',
                    v_sum, v_total, NEW.id;
            END IF;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_check_session_sleep_duration ON session;
CREATE TRIGGER trg_check_session_sleep_duration
AFTER UPDATE OF starttime, endtime, break_duration ON session
FOR EACH ROW
EXECUTE FUNCTION check_session_sleep_duration();

COMMIT;
