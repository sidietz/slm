-- Migration: Integrate Doctor with Point and PostGIS automated air distance calculation
-- Date: 2026-10-18

-- 1. Add is_home flag to point table
ALTER TABLE point ADD COLUMN IF NOT EXISTS is_home BOOLEAN DEFAULT FALSE;

-- Mark Haus Mama (ID 1) as the default home reference point
UPDATE point SET is_home = TRUE WHERE id = 1 OR name ILIKE '%mama%' OR name ILIKE '%home%';

-- 2. Add point_id and base_point_id foreign keys to doctor table
ALTER TABLE doctor
    ADD COLUMN IF NOT EXISTS point_id BIGINT REFERENCES point(id),
    ADD COLUMN IF NOT EXISTS base_point_id BIGINT REFERENCES point(id);

-- Alter air_distance to double precision to match Route.airDistance
ALTER TABLE doctor ALTER COLUMN air_distance TYPE DOUBLE PRECISION;

-- 3. Populate point records for existing 4 doctors (using exact coordinates in Lichtenau, Ansbach, Neuendettelsau)
INSERT INTO point (name, description, latitude, longitude, is_home)
VALUES ('Praxis Dr. Emaine Aliosman', 'Badstraße 3, 91586 Lichtenau', 49.278500, 10.686500, FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO point (name, description, latitude, longitude, is_home)
VALUES ('Praxis Dr. Ulrich Wilms', 'Draisstraße 2, 91522 Ansbach', 49.308700, 10.584700, FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO point (name, description, latitude, longitude, is_home)
VALUES ('Praxis Dr. Bettina Waßer', 'Neuendettelsau', 49.285000, 10.788000, FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO point (name, description, latitude, longitude, is_home)
VALUES ('Praxis Dr. Vera Gabriel', 'Feuchtwanger Str. 38, 91522 Ansbach', 49.296800, 10.564500, FALSE)
ON CONFLICT DO NOTHING;

-- Link existing doctors to their corresponding clinic points
UPDATE doctor SET point_id = (SELECT id FROM point WHERE name = 'Praxis Dr. Emaine Aliosman' LIMIT 1) WHERE lastname ILIKE '%Aliosman%';
UPDATE doctor SET point_id = (SELECT id FROM point WHERE name = 'Praxis Dr. Ulrich Wilms' LIMIT 1) WHERE lastname ILIKE '%Wilms%';
UPDATE doctor SET point_id = (SELECT id FROM point WHERE name = 'Praxis Dr. Bettina Waßer' LIMIT 1) WHERE lastname ILIKE '%Waßer%';
UPDATE doctor SET point_id = (SELECT id FROM point WHERE name = 'Praxis Dr. Vera Gabriel' LIMIT 1) WHERE lastname ILIKE '%Gabriel%';

-- Set default base_point_id to Haus Mama (ID 1)
UPDATE doctor SET base_point_id = (SELECT id FROM point WHERE is_home = TRUE LIMIT 1) WHERE base_point_id IS NULL;

-- 4. Create trigger to compute doctor.air_distance using PostGIS geodesic ST_Distance
CREATE OR REPLACE FUNCTION calculate_doctor_air_distance()
RETURNS TRIGGER AS $$
DECLARE
    practice_geom geography;
    origin_geom geography;
    origin_id bigint;
BEGIN
    -- Determine origin: use base_point_id if specified, otherwise look up the point marked is_home = TRUE
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

-- 5. Create trigger on point table to recalculate doctor.air_distance if coordinates change
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

-- 6. Recalculate air_distance for existing doctors
UPDATE doctor d
SET air_distance = ROUND((ST_Distance(origin.geom, practice.geom) / 1000.0)::numeric, 2)
FROM point origin, point practice
WHERE d.point_id = practice.id
  AND d.base_point_id = origin.id;
