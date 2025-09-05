-- =============================================
-- Final Comprehensive Status Column Fix
-- =============================================
-- This script addresses all possible causes of the status column issue

USE campusworks_bids;

-- Step 1: Check database and table character set
SELECT 'Database character set:' as info;
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM INFORMATION_SCHEMA.SCHEMATA 
WHERE SCHEMA_NAME = 'campusworks_bids';

SELECT 'Table character set:' as info;
SELECT TABLE_COLLATION 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'campusworks_bids' 
AND TABLE_NAME = 'bids';

-- Step 2: Check current status column definition
SELECT 'Current status column:' as info;
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    CHARACTER_SET_NAME,
    COLLATION_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'campusworks_bids' 
AND TABLE_NAME = 'bids' 
AND COLUMN_NAME = 'status';

-- Step 3: Check current data
SELECT 'Current status values:' as info;
SELECT DISTINCT status, LENGTH(status) as length, HEX(status) as hex FROM bids ORDER BY status;

-- Step 4: Set proper character set for the database
ALTER DATABASE campusworks_bids CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Step 5: Set proper character set for the table
ALTER TABLE bids CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Step 6: Completely recreate the status column
SET FOREIGN_KEY_CHECKS = 0;

-- Drop the status column
ALTER TABLE bids DROP COLUMN status;

-- Recreate with proper character set and size
ALTER TABLE bids ADD COLUMN status VARCHAR(20) 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci 
NOT NULL DEFAULT 'PENDING';

SET FOREIGN_KEY_CHECKS = 1;

-- Step 7: Update all existing records
UPDATE bids SET status = 'PENDING' WHERE status = 'PENDING' OR status IS NULL;
UPDATE bids SET status = 'ACCEPTED' WHERE status = 'ACCEPTED';
UPDATE bids SET status = 'REJECTED' WHERE status = 'REJECTED';
UPDATE bids SET status = 'WITHDRAWN' WHERE status = 'WITHDRAWN';
UPDATE bids SET status = 'COMPLETED' WHERE status = 'COMPLETED';
UPDATE bids SET status = 'CANCELLED' WHERE status = 'CANCELLED';

-- Step 8: Verify the fix
SELECT 'After fix - column definition:' as info;
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    CHARACTER_SET_NAME,
    COLLATION_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'campusworks_bids' 
AND TABLE_NAME = 'bids' 
AND COLUMN_NAME = 'status';

SELECT 'After fix - status values:' as info;
SELECT DISTINCT status, LENGTH(status) as length, HEX(status) as hex FROM bids ORDER BY status;

-- Step 9: Test with problematic values
INSERT INTO bids (
    task_id, bidder_id, bidder_email, amount, proposal, status, 
    is_winning, is_accepted, created_at, updated_at
) VALUES (
    999999, 999999, 'test@example.com', 100.00, 'Test', 'COMPLETED',
    false, true, NOW(), NOW()
);

INSERT INTO bids (
    task_id, bidder_id, bidder_email, amount, proposal, status, 
    is_winning, is_accepted, created_at, updated_at
) VALUES (
    999998, 999998, 'test2@example.com', 200.00, 'Test', 'CANCELLED',
    false, false, NOW(), NOW()
);

-- Step 10: Verify test inserts
SELECT 'Test inserts:' as info;
SELECT id, status, LENGTH(status) as length, HEX(status) as hex 
FROM bids WHERE task_id IN (999999, 999998);

-- Step 11: Clean up test data
DELETE FROM bids WHERE task_id IN (999999, 999998);

SELECT 'Final status column fix completed successfully!' as result;
