-- Fix bids table status column size issue
-- The status column needs to accommodate enum values like COMPLETED, CANCELLED

-- Check current column size and data
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'bids' AND COLUMN_NAME = 'status';

-- Check current data in status column
SELECT DISTINCT status, LENGTH(status) as status_length 
FROM bids 
ORDER BY status;

-- Alter the status column to ensure it can handle all enum values
-- Using VARCHAR(20) to be safe (COMPLETED=9, CANCELLED=9, etc.)
ALTER TABLE bids MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Verify the change
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'bids' AND COLUMN_NAME = 'status';

-- Clean up any problematic data
UPDATE bids SET status = 'PENDING' WHERE status = 'PENDING';
UPDATE bids SET status = 'ACCEPTED' WHERE status = 'ACCEPTED';
UPDATE bids SET status = 'REJECTED' WHERE status = 'REJECTED';
UPDATE bids SET status = 'WITHDRAWN' WHERE status = 'WITHDRAWN';
UPDATE bids SET status = 'COMPLETED' WHERE status = 'COMPLETED';
UPDATE bids SET status = 'CANCELLED' WHERE status = 'CANCELLED';

-- Final verification
SELECT DISTINCT status, LENGTH(status) as status_length 
FROM bids 
ORDER BY status;
