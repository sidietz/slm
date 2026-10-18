-- Migration 2026-10-18_4: Robust this_years_birthday helper function (make_date + DST-immune + leap-year safe)
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

-- Ensure PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Route Air Distance Triggers (Idempotent update)
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

-- Doctor Air Distance Triggers (Idempotent update)
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
