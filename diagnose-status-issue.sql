-- =============================================
-- Diagnose Status Column Issue
-- =============================================
-- This script will help identify the exact issue with the status column

USE campusworks_bids;

-- 1. Check current column definition
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

-- 2. Check current data in status column
SELECT 
    status, 
    LENGTH(status) as status_length,
    COUNT(*) as count
FROM bids 
GROUP BY status
ORDER BY status;

-- 3. Check if there are any problematic characters
SELECT 
    id,
    status,
    LENGTH(status) as length,
    HEX(status) as hex_value
FROM bids 
WHERE LENGTH(status) > 10 OR HEX(status) LIKE '%00%' OR HEX(status) LIKE '%20%'
LIMIT 10;

-- 4. Show table structure
DESCRIBE bids;

-- 5. Check for any constraints or indexes on status column
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'campusworks_bids' 
AND TABLE_NAME = 'bids' 
AND COLUMN_NAME = 'status';
