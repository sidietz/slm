CREATE TABLE sporttype(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);

CREATE TABLE session(id TEXT PRIMARY KEY, session_type TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'COMPLETED', starttime TIMESTAMP NOT NULL, endtime TIMESTAMP, break_duration INTERVAL NOT NULL DEFAULT '0 seconds', duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, net_duration INTERVAL GENERATED ALWAYS AS (endtime - starttime - break_duration) STORED, focus_rating SMALLINT CHECK (focus_rating BETWEEN 1 AND 5), comment TEXT, outcome_summary TEXT);

CREATE TABLE sport_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, type BIGINT REFERENCES sporttype(id));

CREATE TABLE meditation_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE);

CREATE TABLE human(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, birthday DATE);
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


CREATE TABLE reading_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, book_id BIGINT REFERENCES book(id), start_page_count INTEGER, end_page_count INTEGER, reading_speed REAL);

CREATE TABLE learning_item(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, author TEXT, source source_type, status status_type, progress REAL, start_date DATE, end_date DATE, last_updated TIMESTAMP);
CREATE TABLE learning_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, learning_item_id BIGINT REFERENCES learning_item(id), units_completed INTEGER);

CREATE TABLE platform(id BIGSERIAL PRIMARY KEY, name TEXT);
CREATE TABLE manufacturer(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);
CREATE TABLE device_type(id BIGSERIAL PRIMARY KEY, name TEXT);
CREATE TABLE device(id BIGSERIAL PRIMARY KEY, name TEXT, model_name TEXT,
serial_number TEXT, type BIGINT NOT NULL REFERENCES device_type(id),
purchase_date DATE, price REAL, vendor_id BIGINT NOT NULL REFERENCES vendor(id), manufacturer_id
BIGINT NOT NULL REFERENCES manufacturer(id));

CREATE TABLE platform(id BIGSERIAL PRIMARY KEY, name TEXT);
CREATE TABLE publisher(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);
CREATE TABLE studio(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);

CREATE TABLE game(id BIGSERIAL PRIMARY KEY, title TEXT, series TEXT, publisher_id BIGINT NOT NULL REFERENCES publisher(id), studio_id BIGINT NOT NULL REFERENCES studio(id), price REAL, release_date DATE, last_played DATE);
CREATE TABLE gaming_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, game_id BIGINT NOT NULL REFERENCES game(id), device_id BIGINT REFERENCES device(id), game_mode TEXT);
CREATE TABLE coding_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, project TEXT, repository TEXT, branch TEXT, ticket TEXT, language TEXT);
CREATE TABLE employer(id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE working_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, employer_id BIGINT REFERENCES employer(id), project TEXT, billable BOOLEAN DEFAULT TRUE, hourly_rate NUMERIC(10, 2), task_category TEXT);
CREATE TABLE sleep_session(id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, is_nap BOOLEAN DEFAULT FALSE, deep_sleep_duration INTERVAL, rem_sleep_duration INTERVAL, awake_count INTEGER DEFAULT 0);

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
  LANGUAGE plpgsql AS
$func$
DECLARE
   ret date;
BEGIN
   ret := date_trunc('year' , current_timestamp)
       + (date_trunc('day'  , _dut)
        - date_trunc('year' , _dut));
   RETURN ret;
END
$func$;


