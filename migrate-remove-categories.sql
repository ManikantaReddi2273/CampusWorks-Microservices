-- =============================================
-- Database Migration: Remove Task Categories
-- =============================================
-- This script migrates existing tasks with removed categories
-- (HISTORY, BUSINESS, MEDICINE, LAW) to OTHER category
-- 
-- Run this script BEFORE deploying the updated code
-- =============================================

-- Update existing tasks with removed categories to OTHER
UPDATE campusworks_tasks.tasks 
SET category = 'OTHER', 
    updated_at = NOW()
WHERE category IN ('HISTORY', 'BUSINESS', 'MEDICINE', 'LAW');

-- Show affected records (for verification)
SELECT 
    id,
    title,
    category,
    owner_email,
    created_at
FROM campusworks_tasks.tasks 
WHERE category = 'OTHER'
ORDER BY created_at DESC;

-- Verify no tasks remain with removed categories
SELECT 
    category,
    COUNT(*) as task_count
FROM campusworks_tasks.tasks 
WHERE category IN ('HISTORY', 'BUSINESS', 'MEDICINE', 'LAW')
GROUP BY category;

-- Expected result: 0 rows (all categories should be migrated)
