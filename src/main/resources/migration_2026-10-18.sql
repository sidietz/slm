-- Migration: 2026-10-18
-- Leverage PostGIS to convert Point coordinates to double precision and auto-calculate Route air distance

CREATE EXTENSION IF NOT EXISTS postgis;

-- Helper function to parse DMS (e.g. 49°17'04.49"N) or decimal strings to double precision
CREATE OR REPLACE FUNCTION parse_dms_to_decimal(coord text) RETURNS double precision AS $$
DECLARE
    matches text[];
    d double precision;
    m double precision;
    s double precision;
    hemi text;
    dec double precision;
BEGIN
    IF coord IS NULL OR btrim(coord) = '' THEN
        RETURN NULL;
    END IF;
    -- Check if already numeric decimal
    IF coord ~ '^-?[0-9]+(\.[0-9]+)?$' THEN
        RETURN coord::double precision;
    END IF;
    -- Match DMS format: e.g. 49°17'04.49"N or 49° 17' 04.49" N
    matches := regexp_match(coord, '^([0-9]+)[°\s]+([0-9]+)[''\s]+([0-9]+(?:\.[0-9]+)?)\"[\s]*([NSEW])', 'i');
    IF matches IS NOT NULL THEN
        d := matches[1]::double precision;
        m := matches[2]::double precision;
        s := matches[3]::double precision;
        hemi := upper(matches[4]);
        dec := d + (m / 60.0) + (s / 3600.0);
        IF hemi IN ('S', 'W') THEN
            dec := -dec;
        END IF;
        RETURN dec;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Alter point table columns to double precision
ALTER TABLE point
    ALTER COLUMN latitude TYPE double precision USING parse_dms_to_decimal(latitude),
    ALTER COLUMN longitude TYPE double precision USING parse_dms_to_decimal(longitude);

-- Add generated PostGIS geography column (lon, lat order)
ALTER TABLE point
    ADD COLUMN IF NOT EXISTS geom geography(Point, 4326)
    GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography) STORED;

-- Create spatial index
CREATE INDEX IF NOT EXISTS idx_point_geom ON point USING GIST (geom);

-- Route air distance calculation trigger function
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

-- Point update trigger to recalculate connected routes if point coordinates change
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

-- Recalculate existing routes
UPDATE route r
SET air_distance = ROUND((ST_Distance(p1.geom, p2.geom) / 1000.0)::numeric, 2)
FROM point p1, point p2
WHERE r.start_point = p1.id AND r.end_point = p2.id;
