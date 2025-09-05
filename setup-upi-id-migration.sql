-- =============================================
-- UPI ID Migration Script for Bidding Service
-- =============================================
-- This script adds UPI ID related columns to the existing bids table
-- Run this script if you have an existing bidding database

USE campusworks_bids;

-- Add UPI ID related columns to bids table
-- Note: MySQL prior to 8.0.19 does not support IF EXISTS on ADD COLUMN within ALTER TABLE.
-- Run these individually or ignore errors if the column already exists.

-- Add UPI ID column
ALTER TABLE bids ADD COLUMN upi_id VARCHAR(255);

-- Add UPI ID viewed flag
ALTER TABLE bids ADD COLUMN upi_id_viewed BOOLEAN DEFAULT FALSE;

-- Add UPI ID submitted timestamp
ALTER TABLE bids ADD COLUMN upi_id_submitted_at DATETIME;

-- Add UPI ID viewed timestamp
ALTER TABLE bids ADD COLUMN upi_id_viewed_at DATETIME;

-- Fix status column size to accommodate COMPLETED and CANCELLED enum values
ALTER TABLE bids MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Add index for UPI ID viewed flag for better query performance
ALTER TABLE bids ADD INDEX idx_upi_id_viewed (upi_id_viewed);

-- Verify the changes
DESCRIBE bids;

-- Show the new columns
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'campusworks_bids' 
AND TABLE_NAME = 'bids' 
AND COLUMN_NAME LIKE '%upi%';

SELECT 'UPI ID migration completed successfully' as Status;
