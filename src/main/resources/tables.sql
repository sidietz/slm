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


