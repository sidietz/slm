CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE sporttype(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);

CREATE TABLE session(id UUID PRIMARY KEY DEFAULT uuidv7(), session_type TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'COMPLETED', starttime TIMESTAMP NOT NULL, endtime TIMESTAMP, break_duration INTERVAL NOT NULL DEFAULT '0 seconds', duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, net_duration INTERVAL GENERATED ALWAYS AS (endtime - starttime - break_duration) STORED, focus_rating SMALLINT CHECK (focus_rating BETWEEN 1 AND 5), comment TEXT, outcome_summary TEXT);

CREATE TABLE sport_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, type BIGINT REFERENCES sporttype(id));

CREATE TABLE meditation_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE);

CREATE TABLE human(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, birthday DATE, category human_category, death_date DATE);
CREATE TABLE human_event (
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
CREATE TABLE address(id BIGSERIAL PRIMARY KEY, human_id BIGSERIAL NOT NULL REFERENCES human(id), street TEXT, streetnumber INTEGER, plz CHAR(5), place TEXT, country TEXT);

CREATE TABLE calendar_event(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);
CREATE TABLE sleeptime(id BIGSERIAL PRIMARY KEY, gotobed TIMESTAMP, standup TIMESTAMP, sleeptime INTERVAL GENERATED ALWAYS AS (standup - gotobed) STORED);

CREATE TABLE train_station(id BIGSERIAL PRIMARY KEY, ds100 TEXT, name TEXT);
CREATE TABLE train_trip(id BIGSERIAL PRIMARY KEY, line TEXT, origin_id BIGSERIAL NOT NULL REFERENCES train_station(id), destination_id BIGSERIAL NOT NULL REFERENCES train_station(id), starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE activity_type(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE activity(id BIGSERIAL PRIMARY KEY, type BIGINT NOT NULL REFERENCES activity_type(id), title TEXT, location TEXT, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE vendor(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE purchase(id BIGSERIAL PRIMARY KEY, title TEXT, vendor BIGSERIAL NOT NULL REFERENCES vendor(id), price REAL, purchase_date DATE);

CREATE TABLE habit(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE, description TEXT);
CREATE TABLE habit_entry(id BIGSERIAL PRIMARY KEY, habit BIGINT NOT NULL REFERENCES habit(id), last_done TIMESTAMP);

CREATE TABLE gratitude(id BIGSERIAL PRIMARY KEY, created_at DATE, description TEXT);
CREATE TABLE diary(id BIGSERIAL PRIMARY KEY, created_at DATE, description TEXT);

CREATE TABLE trainline(id BIGSERIAL PRIMARY KEY, name TEXT, description TEXT);
CREATE TABLE trainstation2(id BIGSERIAL PRIMARY KEY, ds100 TEXT, name TEXT);
CREATE TABLE line_station(line_id BIGINT NOT NULL REFERENCES trainline(id), station_id BIGINT NOT NULL REFERENCES trainstation2(id));

CREATE TABLE train_trip2(id BIGSERIAL PRIMARY KEY, line_id BIGSERIAL NOT NULL REFERENCES trainline(id),
origin_id BIGSERIAL NOT NULL REFERENCES trainstation2(id), destination_id BIGSERIAL NOT NULL REFERENCES trainstation2(id),
starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE mood(id BIGSERIAL PRIMARY KEY, tracked_at DATE UNIQUE NOT NULL, happiness INTEGER, impetus INTEGER, stress INTEGER,
CHECK (happiness BETWEEN 1 AND 10), CHECK (impetus BETWEEN 1 AND 10), CHECK (stress BETWEEN 1 AND 10));

CREATE TABLE author(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, birthday DATE);
CREATE TABLE press(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);

CREATE TABLE book(id BIGSERIAL PRIMARY KEY, isbn VARCHAR(13) UNIQUE, title TEXT, series TEXT, page_count INTEGER,
price REAL, buy_date DATE, last_read DATE, author_id BIGSERIAL NOT NULL REFERENCES author(id), press_id BIGSERIAL NOT NULL REFERENCES press(id),
book_type book_type, book_genre book_genre);

CREATE TABLE point(id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL, description TEXT, latitude DOUBLE PRECISION NOT NULL, longitude DOUBLE PRECISION NOT NULL, is_home BOOLEAN DEFAULT FALSE, geom geography(Point, 4326) GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography) STORED);
CREATE INDEX IF NOT EXISTS idx_point_geom ON point USING GIST (geom);
CREATE TABLE route(id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL, description TEXT, start_point BIGINT NOT NULL REFERENCES point(id), end_point BIGINT NOT NULL REFERENCES point(id), air_distance DOUBLE PRECISION, drive_distance REAL NOT NULL);
CREATE TABLE car(id BIGSERIAL PRIMARY KEY, vin TEXT, name TEXT, model_name TEXT, key_number TEXT, manufacturer_id BIGINT NOT NULL REFERENCES manufacturer(id), vendor_id BIGINT NOT NULL REFERENCES vendor(id), purchase_date DATE, approval_date DATE, manufacturing_date DATE, purchase_price REAL, sell_price REAL, purchase_kilometers BIGINT, sell_kilometers BIGINT, price_per_100_km REAL);
CREATE TABLE car_trip(id BIGSERIAL PRIMARY KEY, car_id BIGSERIAL NOT NULL REFERENCES car(id), route_id BIGSERIAL NOT NULL REFERENCES route(id), is_way_back BOOLEAN, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);


CREATE TABLE reading_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, book_id BIGINT REFERENCES book(id), start_page_count INTEGER, end_page_count INTEGER, reading_speed REAL);

CREATE TABLE learning_item(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, author TEXT, source source_type, status status_type, progress REAL, start_date DATE, end_date DATE, last_updated TIMESTAMP);
CREATE TABLE learning_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, learning_item_id BIGINT REFERENCES learning_item(id), units_completed INTEGER);

CREATE TABLE platform(id BIGSERIAL PRIMARY KEY, name TEXT);
CREATE TABLE manufacturer(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);
CREATE TABLE device_type(id BIGSERIAL PRIMARY KEY, name TEXT);
CREATE TABLE device(id BIGSERIAL PRIMARY KEY, name TEXT, model_name TEXT,
serial_number TEXT, type BIGINT NOT NULL REFERENCES device_type(id),
purchase_date DATE, price REAL, vendor_id BIGINT NOT NULL REFERENCES vendor(id), manufacturer_id
BIGINT NOT NULL REFERENCES manufacturer(id));

CREATE TABLE publisher(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);
CREATE TABLE studio(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);

CREATE TABLE game(id BIGSERIAL PRIMARY KEY, title TEXT, series TEXT, publisher_id BIGINT NOT NULL REFERENCES publisher(id), studio_id BIGINT NOT NULL REFERENCES studio(id), price REAL, release_date DATE, last_played DATE);
CREATE TABLE gaming_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, game_id BIGINT NOT NULL REFERENCES game(id), device_id BIGINT REFERENCES device(id), game_mode TEXT);
CREATE TABLE coding_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, project TEXT, repository TEXT, branch TEXT, ticket TEXT, language TEXT);
CREATE TABLE employer(id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE working_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, employer_id BIGINT REFERENCES employer(id), project TEXT, billable BOOLEAN DEFAULT TRUE, hourly_rate NUMERIC(10, 2), task_category TEXT);
CREATE TABLE sleep_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, is_nap BOOLEAN DEFAULT FALSE, deep_sleep_duration INTERVAL, rem_sleep_duration INTERVAL, awake_count INTEGER DEFAULT 0);
CREATE TABLE course (id BIGSERIAL PRIMARY KEY, title TEXT NOT NULL, code TEXT, semester TEXT, ects_credits NUMERIC(3, 1), lecturer_id BIGINT REFERENCES human(id) ON DELETE SET NULL, default_room TEXT, meeting_link TEXT, syllabus TEXT, exam_date TIMESTAMP, exam_location TEXT, grade NUMERIC(3, 2), exam_passed BOOLEAN, status status_type DEFAULT 'IN_PROGRESS', created_at TIMESTAMP, last_updated TIMESTAMP);
CREATE TABLE lecture_session (id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, course_id BIGINT REFERENCES course(id) ON DELETE SET NULL, lecture_number INTEGER, topic TEXT, room TEXT, recording_url TEXT, slides_url TEXT);
CREATE TABLE lecture_note (id UUID PRIMARY KEY DEFAULT uuidv7(), course_id BIGINT REFERENCES course(id) ON DELETE CASCADE, lecture_session_id UUID REFERENCES lecture_session(id) ON DELETE SET NULL, title TEXT NOT NULL, lecture_number INTEGER, lecture_date DATE, content_markdown TEXT, key_concepts TEXT, summary TEXT, has_homework BOOLEAN DEFAULT FALSE, homework_todo TEXT, exam_relevance_rating INTEGER, created_at TIMESTAMP, last_updated TIMESTAMP);
CREATE INDEX IF NOT EXISTS idx_course_lecturer ON course(lecturer_id);
CREATE INDEX IF NOT EXISTS idx_course_semester ON course(semester);
CREATE INDEX IF NOT EXISTS idx_lecture_session_course ON lecture_session(course_id);
CREATE INDEX IF NOT EXISTS idx_lecture_note_course ON lecture_note(course_id);
CREATE INDEX IF NOT EXISTS idx_lecture_note_session ON lecture_note(lecture_session_id);

CREATE TABLE contractor(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE contract(id BIGSERIAL PRIMARY KEY, title TEXT, contractor_id BIGINT REFERENCES contractor(id), fee REAL, start_date DATE, end_date DATE, active BOOL);

CREATE TABLE speciality(id BIGSERIAL PRIMARY KEY, title TEXT UNIQUE);
CREATE TABLE doctor(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, speciality BIGINT NOT NULL REFERENCES speciality(id), place TEXT, city TEXT, plz TEXT, point_id BIGINT REFERENCES point(id), base_point_id BIGINT REFERENCES point(id), air_distance DOUBLE PRECISION);
CREATE TABLE appointment(id BIGSERIAL PRIMARY KEY, doctor_id BIGINT NOT NULL REFERENCES doctor(id), title TEXT, location TEXT, departure TIMESTAMP, arrival TIMESTAMP, travel_duration INTERVAL GENERATED ALWAYS AS (arrival - departure) STORED, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, arrival_home TIMESTAMP, complete_duration INTERVAL GENERATED ALWAYS AS (arrival_home - departure) STORED, comment TEXT);

CREATE TABLE selfie(id BIGSERIAL PRIMARY KEY, created_at DATE, location TEXT);


-- LearningItem	id, userId, title, author/creator, description, coverUrl, sourceType (BOOK/COURSE/PODCAST), externalId, status (PLANNED, IN_PROGRESS, COMPLETED), progressPercent, startDate, endDate, lastUpdate
-- One table is enough; you can store a JSON blob for source‑specific data if needed.

-- SELECT habit, last_done, last_done - LAG(last_done) OVER () as diff FROM habit_entry WHERE habit = 2;

CREATE OR REPLACE FUNCTION this_years_birthday(_dut date)
  RETURNS date
  LANGUAGE plpgsql STABLE AS
$func$
DECLARE
   cur_year int := EXTRACT(YEAR FROM current_date);
   b_month int := EXTRACT(MONTH FROM _dut);
   b_day int := EXTRACT(DAY FROM _dut);
BEGIN
   IF _dut IS NULL THEN
      RETURN NULL;
   END IF;
   IF b_month = 2 AND b_day = 29 THEN
      IF (cur_year % 4 = 0 AND (cur_year % 100 != 0 OR cur_year % 400 = 0)) THEN
         RETURN make_date(cur_year, 2, 29);
      ELSE
         RETURN make_date(cur_year, 2, 28);
      END IF;
   END IF;
   RETURN make_date(cur_year, b_month, b_day);
END
$func$;

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

-- Route Air Distance Triggers
CREATE OR REPLACE FUNCTION calculate_route_air_distance()
RETURNS TRIGGER AS $$
DECLARE
    start_geom geography;
    end_geom geography;
BEGIN
    SELECT geom INTO start_geom FROM point WHERE id = NEW.start_point;
    SELECT geom INTO end_geom FROM point WHERE id = NEW.end_point;
    
    IF start_geom IS NOT NULL AND end_geom IS NOT NULL THEN
        NEW.air_distance := ROUND((ST_Distance(start_geom, end_geom) / 1000.0)::numeric, 2);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_route_air_distance ON route;
CREATE TRIGGER trg_route_air_distance
BEFORE INSERT OR UPDATE OF start_point, end_point ON route
FOR EACH ROW
EXECUTE FUNCTION calculate_route_air_distance();

CREATE OR REPLACE FUNCTION update_routes_on_point_change()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE route r
    SET air_distance = ROUND((ST_Distance(p1.geom, p2.geom) / 1000.0)::numeric, 2)
    FROM point p1, point p2
    WHERE r.start_point = p1.id
      AND r.end_point = p2.id
      AND (p1.id = NEW.id OR p2.id = NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_point_update_route_air_distance ON point;
CREATE TRIGGER trg_point_update_route_air_distance
AFTER UPDATE OF latitude, longitude ON point
FOR EACH ROW
EXECUTE FUNCTION update_routes_on_point_change();

-- Doctor Air Distance Triggers
CREATE OR REPLACE FUNCTION calculate_doctor_air_distance()
RETURNS TRIGGER AS $$
DECLARE
    practice_geom geography;
    origin_geom geography;
    origin_id bigint;
BEGIN
    origin_id := NEW.base_point_id;
    IF origin_id IS NULL THEN
        SELECT id INTO origin_id FROM point WHERE is_home = TRUE LIMIT 1;
    END IF;

    IF NEW.point_id IS NOT NULL AND origin_id IS NOT NULL THEN
        SELECT geom INTO practice_geom FROM point WHERE id = NEW.point_id;
        SELECT geom INTO origin_geom FROM point WHERE id = origin_id;

        IF practice_geom IS NOT NULL AND origin_geom IS NOT NULL THEN
            NEW.air_distance := ROUND((ST_Distance(origin_geom, practice_geom) / 1000.0)::numeric, 2);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_doctor_air_distance ON doctor;
CREATE TRIGGER trg_doctor_air_distance
BEFORE INSERT OR UPDATE OF point_id, base_point_id ON doctor
FOR EACH ROW
EXECUTE FUNCTION calculate_doctor_air_distance();

CREATE OR REPLACE FUNCTION update_doctor_air_distances_on_point_change()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE doctor d
    SET air_distance = ROUND((ST_Distance(origin.geom, practice.geom) / 1000.0)::numeric, 2)
    FROM point origin, point practice
    WHERE d.point_id = practice.id
      AND d.base_point_id = origin.id
      AND (practice.id = NEW.id OR origin.id = NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_point_update_doctor_air_distance ON point;
CREATE TRIGGER trg_point_update_doctor_air_distance
AFTER UPDATE OF latitude, longitude ON point
FOR EACH ROW
EXECUTE FUNCTION update_doctor_air_distances_on_point_change();



