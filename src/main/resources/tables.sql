CREATE TABLE sporttype(id BIGSERIAL PRIMARY KEY, name TEXT UNIQUE);

CREATE TABLE sportsession(id BIGSERIAL PRIMARY KEY, type BIGINT NOT NULL REFERENCES sporttype(id), starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED, comment TEXT);

CREATE TABLE meditationsession(id BIGSERIAL PRIMARY KEY, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);

CREATE TABLE human(id BIGSERIAL PRIMARY KEY, firstname TEXT, lastname TEXT, birthday DATE);
CREATE TABLE address(id BIGSERIAL PRIMARY KEY, human_id BIGSERIAL NOT NULL REFERENCES human(id), street TEXT, streetnumber INTEGER, plz CHAR(5), place TEXT, country TEXT);

CREATE TABLE calendar_event(id BIGSERIAL PRIMARY KEY, title TEXT, description TEXT, starttime TIMESTAMP, endtime TIMESTAMP, duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED);


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


