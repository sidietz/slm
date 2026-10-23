BEGIN;

DO $$ BEGIN
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
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

ALTER TABLE car ADD COLUMN IF NOT EXISTS target_sell_date DATE;
ALTER TABLE car ADD COLUMN IF NOT EXISTS residual_value NUMERIC(10, 2);

CREATE TABLE IF NOT EXISTS car_expense (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    car_id BIGINT NOT NULL REFERENCES car(id) ON DELETE CASCADE,
    expense_type car_expense_type NOT NULL,
    expense_date DATE NOT NULL,
    odometer NUMERIC(10, 2),
    amount NUMERIC(10, 2) NOT NULL,
    title TEXT NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_car_expense_car_date ON car_expense(car_id, expense_date DESC);
CREATE INDEX IF NOT EXISTS idx_car_expense_type ON car_expense(expense_type);

COMMIT;
