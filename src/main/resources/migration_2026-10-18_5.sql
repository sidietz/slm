-- Migration 2026-10-18_5: Life Events, Anniversaries & Memorials System

-- 1. Create event_type enum if not exists
DO $$ BEGIN
    CREATE TYPE event_type AS ENUM ('BIRTHDAY', 'MEMORIAL', 'WEDDING_ANNIVERSARY', 'RELATIONSHIP_ANNIVERSARY', 'WORK_ANNIVERSARY', 'CUSTOM');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- 2. Add death_date to human table
ALTER TABLE human ADD COLUMN IF NOT EXISTS death_date DATE;

-- 3. SQL helper functions for annual event calculation
CREATE OR REPLACE FUNCTION next_annual_occurrence(_date date)
  RETURNS date
  LANGUAGE plpgsql STABLE AS
$func$
DECLARE
   cur_date date := current_date;
   cur_year int := EXTRACT(YEAR FROM cur_date);
   b_month int := EXTRACT(MONTH FROM _date);
   b_day int := EXTRACT(DAY FROM _date);
   this_year_occ date := this_years_birthday(_date);
   next_year int := cur_year + 1;
BEGIN
   IF _date IS NULL THEN
      RETURN NULL;
   END IF;
   IF this_year_occ >= cur_date THEN
      RETURN this_year_occ;
   END IF;
   IF b_month = 2 AND b_day = 29 THEN
      IF (next_year % 4 = 0 AND (next_year % 100 != 0 OR next_year % 400 = 0)) THEN
         RETURN make_date(next_year, 2, 29);
      ELSE
         RETURN make_date(next_year, 2, 28);
      END IF;
   END IF;
   RETURN make_date(next_year, b_month, b_day);
END
$func$;

CREATE OR REPLACE FUNCTION days_until_annual_occurrence(_date date)
  RETURNS integer
  LANGUAGE sql STABLE AS
$$
  SELECT (next_annual_occurrence(_date) - current_date);
$$;

-- 4. Create human_event table
CREATE TABLE IF NOT EXISTS human_event (
    id BIGSERIAL PRIMARY KEY,
    human_id BIGINT NOT NULL REFERENCES human(id) ON DELETE CASCADE,
    secondary_human_id BIGINT REFERENCES human(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    event_type event_type NOT NULL DEFAULT 'CUSTOM',
    event_date DATE NOT NULL,
    annual_recurrence BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_human_event_human ON human_event(human_id);
CREATE INDEX IF NOT EXISTS idx_human_event_secondary_human ON human_event(secondary_human_id);
CREATE INDEX IF NOT EXISTS idx_human_event_type ON human_event(event_type);

-- 5. Auto-migrate existing human birthdays into human_event
INSERT INTO human_event (human_id, title, event_type, event_date, annual_recurrence)
SELECT id, 'Birthday', 'BIRTHDAY'::event_type, birthday, TRUE
FROM human
WHERE birthday IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM human_event he 
      WHERE he.human_id = human.id AND he.event_type = 'BIRTHDAY'
  );
