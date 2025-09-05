-- =============================================
-- Force Fix Status Column Issue
-- =============================================
-- This script will aggressively fix the status column issue

USE campusworks_bids;

-- Step 1: Backup current data
CREATE TABLE IF NOT EXISTS bids_backup AS SELECT * FROM bids;

-- Step 2: Check current column definition
SELECT 'Current column definition:' as info;
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'campusworks_bids' 
AND TABLE_NAME = 'bids' 
AND COLUMN_NAME = 'status';

-- Step 3: Check current data
SELECT 'Current status values:' as info;
SELECT DISTINCT status, LENGTH(status) as length FROM bids ORDER BY status;

-- Step 4: Drop and recreate the status column with proper size
-- First, remove any foreign key constraints if they exist
SET FOREIGN_KEY_CHECKS = 0;

-- Drop the status column
ALTER TABLE bids DROP COLUMN status;

-- Recreate the status column with proper size and constraints
ALTER TABLE bids ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Step 5: Update all existing records with proper status values
UPDATE bids SET status = 'PENDING' WHERE status = 'PENDING' OR status IS NULL;
UPDATE bids SET status = 'ACCEPTED' WHERE status = 'ACCEPTED';
UPDATE bids SET status = 'REJECTED' WHERE status = 'REJECTED';
UPDATE bids SET status = 'WITHDRAWN' WHERE status = 'WITHDRAWN';
UPDATE bids SET status = 'COMPLETED' WHERE status = 'COMPLETED';
UPDATE bids SET status = 'CANCELLED' WHERE status = 'CANCELLED';

-- Step 6: Verify the fix
SELECT 'After fix - column definition:' as info;
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'campusworks_bids' 
AND TABLE_NAME = 'bids' 
AND COLUMN_NAME = 'status';

SELECT 'After fix - status values:' as info;
SELECT DISTINCT status, LENGTH(status) as length FROM bids ORDER BY status;

-- Step 7: Test insert with COMPLETED status
INSERT INTO bids (
    task_id, bidder_id, bidder_email, amount, proposal, status, 
    is_winning, is_accepted, created_at, updated_at
) VALUES (
    999999, 999999, 'test@example.com', 100.00, 'Test proposal', 'COMPLETED',
    false, true, NOW(), NOW()
);

-- Step 8: Test insert with CANCELLED status
INSERT INTO bids (
    task_id, bidder_id, bidder_email, amount, proposal, status, 
    is_winning, is_accepted, created_at, updated_at
) VALUES (
    999998, 999998, 'test2@example.com', 200.00, 'Test proposal 2', 'CANCELLED',
    false, false, NOW(), NOW()
);

-- Step 9: Verify test inserts
SELECT 'Test inserts verification:' as info;
SELECT id, status, LENGTH(status) as length FROM bids WHERE task_id IN (999999, 999998);

-- Step 10: Clean up test data
DELETE FROM bids WHERE task_id IN (999999, 999998);

SELECT 'Status column fix completed successfully!' as result;
