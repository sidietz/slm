-- Migration 2026-10-25_1: Add UAV / Drone and Drone Flight Tracking System with PostGIS Geodesics
BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'drone_category') THEN
        CREATE TYPE drone_category AS ENUM ('C0', 'C1', 'C2', 'C3', 'C4');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS drone (
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

CREATE INDEX IF NOT EXISTS idx_drone_category ON drone(category);
CREATE INDEX IF NOT EXISTS idx_drone_manufacturer ON drone(manufacturer_id);
CREATE INDEX IF NOT EXISTS idx_drone_vendor ON drone(vendor_id);

CREATE TABLE IF NOT EXISTS drone_flight (
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

CREATE INDEX IF NOT EXISTS idx_drone_flight_drone ON drone_flight(drone_id);
CREATE INDEX IF NOT EXISTS idx_drone_flight_start ON drone_flight(starttime DESC);
CREATE INDEX IF NOT EXISTS idx_drone_flight_start_geom ON drone_flight USING GIST (start_geom);
CREATE INDEX IF NOT EXISTS idx_drone_flight_landing_geom ON drone_flight USING GIST (landing_geom);

COMMIT;
