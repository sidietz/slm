-- Database Initialization for Simon's Life Manager (SLM)
-- PostgreSQL 18 with PostGIS 3.6 Extension

CREATE EXTENSION IF NOT EXISTS postgis;

-- 1. Helper Functions
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

-- 2. Enums
CREATE TYPE book_genre AS ENUM ('ACTION', 'BIOGRAPHY', 'CHILDREN', 'CRIME', 'ESSAY', 'FANTASY',
'FOODDRINK', 'GRAPHICNOVEL', 'GUIDE', 'HISTORICALFICTION', 'HISTORY', 'HORROR' ,'HUMANITIES',
'HUMOR', 'IT', 'NEWADULT', 'PARENTING', 'PHILOSOPHICFICTION', 'PHOTOGRAPHY', 'RELIGION', 'ROMANCE',
'SCIENCE', 'SCIFI', 'SELFHELP', 'SHORTSTORY', 'THRILLER', 'TRAVEL', 'TRUECRIME', 'YOUNGADULT');

CREATE TYPE book_type AS ENUM ('FICTION', 'NONFICTION');
CREATE TYPE source_type AS ENUM ('BOOK', 'COURSE', 'LECTURE', 'TUTORIAL', 'YOUTUBE');
CREATE TYPE status_type AS ENUM('PLANNED', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE academic_activity_type AS ENUM ('LECTURE', 'PREPARATION', 'LEARNING', 'HOMEWORK', 'LAB', 'PROJECT', 'PRESENTATION');
CREATE TYPE human_category AS ENUM ('FAMILY', 'CLASSMATE', 'FRIEND', 'FRIEND_OF_FRIEND', 'TEACHER', 'LECTURER', 'CELEBRITY', 'POLITICIAN', 'SPD', 'OTHER');
CREATE TYPE event_type AS ENUM ('BIRTHDAY', 'MEMORIAL', 'WEDDING_ANNIVERSARY', 'RELATIONSHIP_ANNIVERSARY', 'WORK_ANNIVERSARY', 'CUSTOM');

CREATE TYPE fuel_type AS ENUM (
    'DIESEL',
    'SUPER_E10',
    'SUPER_E5',
    'SUPER_PLUS',
    'PREMIUM_DIESEL',
    'LPG',
    'CNG',
    'ELECTRIC',
    'HYDROGEN',
    'OTHER'
);

CREATE TYPE fueling_type AS ENUM (
    'FULL',
    'PARTIAL',
    'INITIAL'
);

CREATE TYPE driving_style AS ENUM (
    'ECONOMICAL',
    'NORMAL',
    'FAST'
);

CREATE TYPE tire_type AS ENUM (
    'SUMMER',
    'WINTER',
    'ALL_SEASON'
);

CREATE TYPE station_type AS ENUM (
    'GAS_STATION',
    'CHARGING_STATION',
    'H2_STATION',
    'OTHER'
);

CREATE TYPE plug_type AS ENUM (
    'CCS_TYPE2',
    'CHADEMO',
    'GBT',
    'OTHER',
    'TESLA',
    'TYPE_1',
    'TYPE_2',
    'UNKNOWN'
);

CREATE TYPE car_expense_type AS ENUM (
    'MAINTENANCE',
    'REPAIR',
    'TIRE_CHANGE',
    'OIL_CHANGE',
    'INSURANCE',
    'TAX',
    'INSPECTION',
    'TUNING',
    'ACCESSORIES',
    'PURCHASE_PRICE',
    'OTHER',
    'CARE',
    'LEASING_RATE',
    'REGISTRATION',
    'FINANCING_RATE',
    'REFUND',
    'FINE',
    'PARKING_FEE',
    'TOLL',
    'SPARE_PARTS',
    'CHARGING_BASE_FEE',
    'DIGITAL_SERVICES',
    'GHG_QUOTA'
);

CREATE TYPE drone_category AS ENUM (
    'C0',
    'C1',
    'C2',
    'C3',
    'C4'
);

-- 3. Core Tables & Sessions Hierarchy
CREATE TABLE sporttype(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);

CREATE TABLE session(
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    session_type TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    starttime TIMESTAMPTZ NOT NULL,
    endtime TIMESTAMPTZ,
    break_duration INTERVAL NOT NULL DEFAULT '0 seconds',
    duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED,
    net_duration INTERVAL GENERATED ALWAYS AS (endtime - starttime - break_duration) STORED,
    focus_rating SMALLINT CHECK (focus_rating BETWEEN 1 AND 5),
    comment TEXT,
    outcome_summary TEXT
);

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

CREATE TABLE calendar_event(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, starttime TIMESTAMPTZ, endtime TIMESTAMPTZ, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);
CREATE TABLE sleeptime(id BIGSERIAL PRIMARY KEY, gotobed TIMESTAMPTZ, standup TIMESTAMPTZ, sleeptime INTERVAL GENERATED ALWAYS AS (standup - gotobed) STORED);

CREATE TABLE train_station(id BIGSERIAL PRIMARY KEY, ds100 TEXT, name TEXT);
CREATE TABLE train_trip(id BIGSERIAL PRIMARY KEY, line TEXT, origin_id BIGSERIAL NOT NULL REFERENCES train_station(id), destination_id BIGSERIAL NOT NULL REFERENCES train_station(id), starttime TIMESTAMPTZ, endtime TIMESTAMPTZ, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE activity_type(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE activity(id BIGSERIAL PRIMARY KEY, type BIGINT NOT NULL REFERENCES activity_type(id), title TEXT, location TEXT, starttime TIMESTAMPTZ, endtime TIMESTAMPTZ, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE vendor(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE purchase(id BIGSERIAL PRIMARY KEY, title TEXT, vendor BIGSERIAL NOT NULL REFERENCES vendor(id), price REAL, purchase_date DATE);

CREATE TABLE habit(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE, description TEXT);
CREATE TABLE habit_entry(id BIGSERIAL PRIMARY KEY, habit BIGINT NOT NULL REFERENCES habit(id), last_done TIMESTAMPTZ);

CREATE TABLE gratitude(id BIGSERIAL PRIMARY KEY, created_at DATE, description TEXT);
CREATE TABLE diary(id BIGSERIAL PRIMARY KEY, created_at DATE, description TEXT);

CREATE TABLE trainline(id BIGSERIAL PRIMARY KEY, name TEXT, description TEXT);
CREATE TABLE trainstation2(id BIGSERIAL PRIMARY KEY, ds100 TEXT, name TEXT);
CREATE TABLE line_station(line_id BIGINT NOT NULL REFERENCES trainline(id), station_id BIGINT NOT NULL REFERENCES trainstation2(id));

CREATE TABLE train_trip2(id BIGSERIAL PRIMARY KEY, line_id BIGSERIAL NOT NULL REFERENCES trainline(id),
origin_id BIGSERIAL NOT NULL REFERENCES trainstation2(id), destination_id BIGSERIAL NOT NULL REFERENCES trainstation2(id),
starttime TIMESTAMPTZ, endtime TIMESTAMPTZ, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE mood(id BIGSERIAL PRIMARY KEY, tracked_at DATE UNIQUE NOT NULL, happiness INTEGER, impetus INTEGER, stress INTEGER,
CHECK (happiness BETWEEN 1 AND 10), CHECK (impetus BETWEEN 1 AND 10), CHECK (stress BETWEEN 1 AND 10));

CREATE TABLE author(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, birthday DATE);
CREATE TABLE press(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);

CREATE TABLE book(id BIGSERIAL PRIMARY KEY, isbn VARCHAR(13) UNIQUE, title TEXT, series TEXT, page_count INTEGER,
price REAL, buy_date DATE, last_read DATE, author_id BIGSERIAL NOT NULL REFERENCES author(id), press_id BIGSERIAL NOT NULL REFERENCES press(id),
book_type book_type, book_genre book_genre);

CREATE TABLE point(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    is_home BOOLEAN DEFAULT FALSE,
    geom geography(Point, 4326) GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography) STORED
);
CREATE INDEX IF NOT EXISTS idx_point_geom ON point USING GIST (geom);

CREATE TABLE manufacturer(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);
CREATE TABLE device_type(id BIGSERIAL PRIMARY KEY, name TEXT);
CREATE TABLE device(id BIGSERIAL PRIMARY KEY, name TEXT, model_name TEXT,
serial_number TEXT, type BIGINT NOT NULL REFERENCES device_type(id),
purchase_date DATE, price REAL, vendor_id BIGINT NOT NULL REFERENCES vendor(id), manufacturer_id
BIGINT NOT NULL REFERENCES manufacturer(id));

CREATE TABLE route(id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL, description TEXT, start_point BIGINT NOT NULL REFERENCES point(id), end_point BIGINT NOT NULL REFERENCES point(id), air_distance DOUBLE PRECISION, drive_distance REAL NOT NULL);
CREATE TABLE car(id BIGSERIAL PRIMARY KEY, vin TEXT, name TEXT, model_name TEXT, key_number TEXT, manufacturer_id BIGINT NOT NULL REFERENCES manufacturer(id), vendor_id BIGINT NOT NULL REFERENCES vendor(id), purchase_date DATE, approval_date DATE, manufacturing_date DATE, purchase_price REAL, sell_price REAL, purchase_kilometers BIGINT, sell_kilometers BIGINT, price_per_100_km REAL, target_sell_date DATE, residual_value NUMERIC(10, 2));
CREATE TABLE car_trip(id BIGSERIAL PRIMARY KEY, car_id BIGSERIAL NOT NULL REFERENCES car(id), route_id BIGSERIAL NOT NULL REFERENCES route(id), is_way_back BOOLEAN, starttime TIMESTAMPTZ, endtime TIMESTAMPTZ, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE reading_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, book_id BIGINT REFERENCES book(id), start_page_count INTEGER, end_page_count INTEGER, reading_speed REAL);

CREATE TABLE learning_item(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, author TEXT, source source_type, status status_type, progress REAL, start_date DATE, end_date DATE, last_updated TIMESTAMPTZ);
CREATE TABLE learning_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, learning_item_id BIGINT REFERENCES learning_item(id), units_completed INTEGER);

CREATE TABLE publisher(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);
CREATE TABLE studio(id BIGSERIAL PRIMARY KEY, name TEXT, hq TEXT, founding_date DATE);

CREATE TABLE game(id BIGSERIAL PRIMARY KEY, title TEXT, series TEXT, publisher_id BIGINT NOT NULL REFERENCES publisher(id), studio_id BIGINT NOT NULL REFERENCES studio(id), price REAL, release_date DATE, last_played DATE);
CREATE TABLE gaming_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, game_id BIGINT NOT NULL REFERENCES game(id), device_id BIGINT REFERENCES device(id), game_mode TEXT);
CREATE TABLE coding_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, project TEXT, repository TEXT, branch TEXT, ticket TEXT, language TEXT);

CREATE TABLE employer(id BIGSERIAL PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE working_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, employer_id BIGINT REFERENCES employer(id), project TEXT, billable BOOLEAN DEFAULT TRUE, hourly_rate NUMERIC(10, 2), task_category TEXT);
CREATE TABLE sleep_session(id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, is_nap BOOLEAN DEFAULT FALSE, deep_sleep_duration INTERVAL, rem_sleep_duration INTERVAL, light_sleep_duration INTERVAL, awake_count INTEGER DEFAULT 0);
CREATE TABLE course (id BIGSERIAL PRIMARY KEY, title TEXT NOT NULL, code TEXT, semester TEXT, ects_credits NUMERIC(3, 1), lecturer_id BIGINT REFERENCES human(id) ON DELETE SET NULL, default_room TEXT, meeting_link TEXT, syllabus TEXT, exam_date TIMESTAMPTZ, exam_location TEXT, grade NUMERIC(3, 2), exam_passed BOOLEAN, status status_type DEFAULT 'IN_PROGRESS', created_at TIMESTAMPTZ, last_updated TIMESTAMPTZ);
CREATE TABLE lecture_session (id UUID PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE, course_id BIGINT REFERENCES course(id) ON DELETE SET NULL, lecture_number INTEGER, topic TEXT, room TEXT, recording_url TEXT, slides_url TEXT, activity_type academic_activity_type NOT NULL DEFAULT 'LECTURE');
CREATE TABLE lecture_note (id UUID PRIMARY KEY DEFAULT uuidv7(), course_id BIGINT REFERENCES course(id) ON DELETE CASCADE, lecture_session_id UUID REFERENCES lecture_session(id) ON DELETE SET NULL, title TEXT NOT NULL, lecture_number INTEGER, lecture_date DATE, content_markdown TEXT, key_concepts TEXT, summary TEXT, has_homework BOOLEAN DEFAULT FALSE, homework_todo TEXT, exam_relevance_rating INTEGER, created_at TIMESTAMPTZ, last_updated TIMESTAMPTZ);
CREATE INDEX IF NOT EXISTS idx_course_lecturer ON course(lecturer_id);
CREATE INDEX IF NOT EXISTS idx_course_semester ON course(semester);
CREATE INDEX IF NOT EXISTS idx_lecture_session_course ON lecture_session(course_id);
CREATE INDEX IF NOT EXISTS idx_lecture_session_activity_type ON lecture_session(activity_type);
CREATE INDEX IF NOT EXISTS idx_lecture_note_course ON lecture_note(course_id);
CREATE INDEX IF NOT EXISTS idx_lecture_note_session ON lecture_note(lecture_session_id);

CREATE TABLE contractor(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);
CREATE TABLE contract(id BIGSERIAL PRIMARY KEY, title TEXT, contractor_id BIGINT REFERENCES contractor(id), fee REAL, start_date DATE, initial_end_date DATE, auto_extension BOOL NOT NULL DEFAULT TRUE, auto_extension_months INT NOT NULL DEFAULT 1, is_canceled BOOL NOT NULL DEFAULT FALSE, cancellation_date DATE, termination_date DATE, active BOOL);

CREATE TABLE speciality(id BIGSERIAL PRIMARY KEY, title TEXT UNIQUE);
CREATE TABLE doctor(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, speciality BIGINT NOT NULL REFERENCES speciality(id), place TEXT, city TEXT, plz TEXT, point_id BIGINT REFERENCES point(id), base_point_id BIGINT REFERENCES point(id), air_distance DOUBLE PRECISION);
CREATE TABLE appointment(id BIGSERIAL PRIMARY KEY, doctor_id BIGINT NOT NULL REFERENCES doctor(id), title TEXT, location TEXT, departure TIMESTAMPTZ, arrival TIMESTAMPTZ, travel_duration INTERVAL GENERATED ALWAYS AS (arrival - departure) STORED, starttime TIMESTAMPTZ, endtime TIMESTAMPTZ, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, arrival_home TIMESTAMPTZ, complete_duration INTERVAL GENERATED ALWAYS AS (arrival_home - departure) STORED, comment TEXT);

CREATE TABLE selfie(id BIGSERIAL PRIMARY KEY, created_at DATE, location TEXT);

-- 4. Route Air Distance Triggers
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

-- 5. Doctor Air Distance Triggers
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

DROP TRIGGER IF EXISTS trg_check_sleep_session_duration ON sleep_session;
CREATE TRIGGER trg_check_sleep_session_duration
BEFORE INSERT OR UPDATE ON sleep_session
FOR EACH ROW
EXECUTE FUNCTION check_sleep_session_duration();

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
CREATE CONSTRAINT TRIGGER trg_check_session_sleep_duration
AFTER UPDATE ON session
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION check_session_sleep_duration();

-- Polymorphic Base Station (Native UUIDv7)
CREATE TABLE station (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name TEXT NOT NULL,
    brand TEXT,
    station_type station_type NOT NULL,
    street TEXT,
    house_number TEXT,
    postal_code TEXT,
    city TEXT,
    country VARCHAR(2) CHECK (country ~ '^[A-Z]{2}$'),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    point_id BIGINT REFERENCES point(id) ON DELETE SET NULL,
    note TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_station_name ON station(name);
CREATE INDEX idx_station_country_city ON station(country, city);

-- Gas Station (Combustion fuels with TankenTanken UUID)
CREATE TABLE gas_station (
    id UUID PRIMARY KEY REFERENCES station(id) ON DELETE CASCADE,
    tankentanken_id UUID UNIQUE
);
CREATE INDEX idx_gas_station_tankentanken ON gas_station(tankentanken_id);

-- Charging Station (Electric vehicles)
CREATE TABLE charging_station (
    id UUID PRIMARY KEY REFERENCES station(id) ON DELETE CASCADE,
    operator TEXT
);

-- Charging Point (1:N under Charging Station with individual power ratings)
CREATE TABLE charging_point (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    charging_station_id UUID NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    point_identifier TEXT,
    max_power_kw NUMERIC(6, 1) NOT NULL,
    plug_type plug_type NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_charging_point_station ON charging_point(charging_station_id);

-- Vehicle Refueling Event (Native UUIDv7)
CREATE TABLE refueling (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    car_id BIGINT NOT NULL REFERENCES car(id) ON DELETE CASCADE,
    station_id UUID REFERENCES station(id) ON DELETE SET NULL,
    charging_point_id UUID REFERENCES charging_point(id) ON DELETE SET NULL,
    fueling_date TIMESTAMPTZ NOT NULL,
    odometer NUMERIC(10, 2),
    trip_distance NUMERIC(10, 2),
    liters NUMERIC(8, 2) NOT NULL,
    cost_per_liter NUMERIC(8, 3),
    total_cost NUMERIC(8, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR' CHECK (currency ~ '^[A-Z]{3}$'),
    fuel_type fuel_type,
    fueling_type fueling_type DEFAULT 'FULL',
    driving_style driving_style,
    tire_type tire_type,
    country VARCHAR(2) CHECK (country ~ '^[A-Z]{2}$'),
    location TEXT,
    routes TEXT,
    -- Calculated physical consumption from pump receipt: (liters * 100) / trip_distance
    calculated_consumption NUMERIC(6, 2),
    -- BC = Bordcomputer (Board Computer / On-Board Trip Computer telemetry from vehicle dashboard)
    board_computer_consumption NUMERIC(6, 2), -- BC-Verbrauch (L/100km)
    board_computer_quantity NUMERIC(8, 2),    -- BC-Spritmenge (Liters)
    board_computer_speed NUMERIC(6, 2),       -- BC-Geschwindigkeit (km/h)
    note TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refueling_car_date ON refueling(car_id, fueling_date DESC);
CREATE INDEX idx_refueling_station ON refueling(station_id);

-- Vehicle Operating & Capital Expenses (Spritmonitor Taxonomy, Native UUIDv7)
CREATE TABLE car_expense (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    car_id BIGINT NOT NULL REFERENCES car(id) ON DELETE CASCADE,
    expense_type car_expense_type NOT NULL,
    expense_date DATE NOT NULL,
    odometer NUMERIC(10, 2),
    amount NUMERIC(10, 2) NOT NULL,
    title TEXT NOT NULL,
    note TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_car_expense_car_date ON car_expense(car_id, expense_date DESC);
CREATE INDEX idx_car_expense_type ON car_expense(expense_type);

-- Unmanned Aerial Vehicles (Drones / UAVs) & Flight Logs
CREATE TABLE drone (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name TEXT NOT NULL,
    model_name TEXT,
    category drone_category NOT NULL,
    weight_grams INTEGER NOT NULL,
    buy_date DATE,
    serial_number TEXT,
    uas_operator_id TEXT,
    purchase_price NUMERIC(10, 2),
    manufacturer_id BIGINT REFERENCES manufacturer(id),
    vendor_id BIGINT REFERENCES vendor(id),
    notes TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_drone_category ON drone(category);
CREATE INDEX idx_drone_manufacturer ON drone(manufacturer_id);
CREATE INDEX idx_drone_vendor ON drone(vendor_id);

CREATE TABLE drone_flight (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    drone_id UUID NOT NULL REFERENCES drone(id) ON DELETE CASCADE,
    starttime TIMESTAMPTZ NOT NULL,
    endtime TIMESTAMPTZ NOT NULL,
    duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED,
    start_latitude DOUBLE PRECISION,
    start_longitude DOUBLE PRECISION,
    landing_latitude DOUBLE PRECISION,
    landing_longitude DOUBLE PRECISION,
    start_geom geography(Point, 4326) GENERATED ALWAYS AS (
        CASE WHEN start_latitude IS NOT NULL AND start_longitude IS NOT NULL 
             THEN ST_SetSRID(ST_MakePoint(start_longitude, start_latitude), 4326)::geography 
             ELSE NULL END
    ) STORED,
    landing_geom geography(Point, 4326) GENERATED ALWAYS AS (
        CASE WHEN landing_latitude IS NOT NULL AND landing_longitude IS NOT NULL 
             THEN ST_SetSRID(ST_MakePoint(landing_longitude, landing_latitude), 4326)::geography 
             ELSE NULL END
    ) STORED,
    air_distance DOUBLE PRECISION GENERATED ALWAYS AS (
        CASE WHEN start_latitude IS NOT NULL AND landing_latitude IS NOT NULL 
             THEN ST_Distance(
                 ST_SetSRID(ST_MakePoint(start_longitude, start_latitude), 4326)::geography, 
                 ST_SetSRID(ST_MakePoint(landing_longitude, landing_latitude), 4326)::geography
             )
             ELSE NULL END
    ) STORED,
    max_altitude_meters DOUBLE PRECISION,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_drone_flight_drone ON drone_flight(drone_id);
CREATE INDEX idx_drone_flight_start ON drone_flight(starttime DESC);
CREATE INDEX idx_drone_flight_start_geom ON drone_flight USING GIST (start_geom);
CREATE INDEX idx_drone_flight_landing_geom ON drone_flight USING GIST (landing_geom);


