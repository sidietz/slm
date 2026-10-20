BEGIN;

-- 1. Create Enums if they do not exist
DO $$ BEGIN
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
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE fueling_type AS ENUM (
        'FULL',
        'PARTIAL',
        'INITIAL'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE driving_style AS ENUM (
        'ECONOMICAL',
        'NORMAL',
        'FAST'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE tire_type AS ENUM (
        'SUMMER',
        'WINTER',
        'ALL_SEASON'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE station_type AS ENUM (
        'GAS_STATION',
        'CHARGING_STATION',
        'H2_STATION',
        'OTHER'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
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
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. Create Station Tables
CREATE TABLE IF NOT EXISTS station (
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_station_name ON station(name);
CREATE INDEX IF NOT EXISTS idx_station_country_city ON station(country, city);

CREATE TABLE IF NOT EXISTS gas_station (
    id UUID PRIMARY KEY REFERENCES station(id) ON DELETE CASCADE,
    tankentanken_id UUID UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_gas_station_tankentanken ON gas_station(tankentanken_id);

CREATE TABLE IF NOT EXISTS charging_station (
    id UUID PRIMARY KEY REFERENCES station(id) ON DELETE CASCADE,
    operator TEXT
);

CREATE TABLE IF NOT EXISTS charging_point (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    charging_station_id UUID NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    point_identifier TEXT,
    max_power_kw NUMERIC(6, 1) NOT NULL,
    plug_type plug_type NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_charging_point_station ON charging_point(charging_station_id);

-- 3. Create Refueling Table
CREATE TABLE IF NOT EXISTS refueling (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    car_id BIGINT NOT NULL REFERENCES car(id) ON DELETE CASCADE,
    station_id UUID REFERENCES station(id) ON DELETE SET NULL,
    charging_point_id UUID REFERENCES charging_point(id) ON DELETE SET NULL,
    fueling_date TIMESTAMP NOT NULL,
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
    calculated_consumption NUMERIC(6, 2),
    board_computer_consumption NUMERIC(6, 2),
    board_computer_quantity NUMERIC(8, 2),
    board_computer_speed NUMERIC(6, 2),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refueling_car_date ON refueling(car_id, fueling_date DESC);
CREATE INDEX IF NOT EXISTS idx_refueling_station ON refueling(station_id);

COMMIT;
