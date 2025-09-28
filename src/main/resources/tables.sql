CREATE TABLE sporttype(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);

CREATE TABLE sportsession(id BIGSERIAL PRIMARY KEY, type BIGINT NOT NULL REFERENCES sporttype(id), starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, comment TEXT);

CREATE TABLE meditationsession(id BIGSERIAL PRIMARY KEY, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE human(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, birthday DATE);
CREATE TABLE address(id BIGSERIAL PRIMARY KEY, human_id BIGSERIAL NOT NULL REFERENCES human(id), street TEXT, streetnumber INTEGER, plz CHAR(5), place TEXT, country TEXT);

CREATE TABLE calendar_event(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE train_station(id BIGSERIAL PRIMARY KEY, ds100 TEXT, name TEXT);
CREATE TABLE train_trip(id BIGSERIAL PRIMARY KEY, line TEXT, origin_id BIGSERIAL NOT NULL REFERENCES train_station(id), destination_id BIGSERIAL NOT NULL REFERENCES train_station(id), starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE activity_type(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE activity(id BIGSERIAL PRIMARY KEY, type BIGINT NOT NULL REFERENCES activity_type(id), title TEXT, location TEXT, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE vendor(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE purchase(id BIGSERIAL PRIMARY KEY, title TEXT, vendor BIGSERIAL NOT NULL REFERENCES vendor(id), price REAL, purchase_date DATE);

CREATE TABLE habit(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE, description TEXT);
CREATE TABLE habit_entry(id BIGSERIAL PRIMARY KEY, habit BIGINT NOT NULL REFERENCES habit(id), last_done TIMESTAMP);

CREATE TABLE gratitude(id BIGSERIAL PRIMARY KEY, created_at DATE, description TEXT);

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

CREATE TABLE readingsession(id BIGSERIAL PRIMARY KEY, book_id BIGSERIAL REFERENCES book(id), start_page_count INTEGER, end_page_count INTEGER, reading_speed REAL, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE learning_item(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, author TEXT, source source_type, status status_type, progress REAL, start_date DATE, end_date DATE, last_updated TIMESTAMP);
CREATE TABLE learning_session(id BIGSERIAL PRIMARY KEY, learning_item_id BIGINT REFERENCES learning_item(id), starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, comment TEXT);

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
CREATE TABLE gaming_session(id BIGSERIAL PRIMARY KEY, game_id BIGINT NOT NULL REFERENCES game(id), device_id BIGINT NOT NULL REFERENCES device(id), starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, comment TEXT);

CREATE TABLE contractor(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE contract(id BIGSERIAL PRIMARY KEY, title TEXT, contractor_id BIGINT REFERENCES contractor(id), fee REAL, start_date DATE, end_date DATE, active BOOL);

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


