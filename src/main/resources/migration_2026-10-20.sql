BEGIN;

-- 1. Rename end_date to initial_end_date if present
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'contract' AND column_name = 'end_date'
    ) THEN
        ALTER TABLE contract RENAME COLUMN end_date TO initial_end_date;
    END IF;
END $$;

-- 2. Add auto-extension and cancellation tracking columns
ALTER TABLE contract ADD COLUMN IF NOT EXISTS auto_extension BOOL NOT NULL DEFAULT TRUE;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS auto_extension_months INT NOT NULL DEFAULT 1;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS is_canceled BOOL NOT NULL DEFAULT FALSE;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS cancellation_date DATE;
ALTER TABLE contract ADD COLUMN IF NOT EXISTS termination_date DATE;

-- 3. Populate termination_date for already inactive contracts that had an initial_end_date
UPDATE contract 
SET termination_date = initial_end_date, 
    is_canceled = TRUE 
WHERE active = FALSE AND initial_end_date IS NOT NULL AND termination_date IS NULL;

COMMIT;
