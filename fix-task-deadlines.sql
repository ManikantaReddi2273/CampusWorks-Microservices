-- Fix Task Deadlines Script
-- This script fixes tasks that have incorrect completion deadlines

-- Update tasks where completion_deadline is NULL or in the past
UPDATE tasks 
SET completion_deadline = DATE_ADD(bidding_deadline, INTERVAL 7 DAY)
WHERE completion_deadline IS NULL 
   OR completion_deadline <= NOW()
   OR completion_deadline <= bidding_deadline;

-- Update tasks where completion_deadline is too close to bidding_deadline (less than 1 hour)
UPDATE tasks 
SET completion_deadline = DATE_ADD(bidding_deadline, INTERVAL 7 DAY)
WHERE completion_deadline <= DATE_ADD(bidding_deadline, INTERVAL 1 HOUR);

-- Show the updated tasks
SELECT 
    id,
    title,
    bidding_deadline,
    completion_deadline,
    status,
    created_at
FROM tasks 
ORDER BY created_at DESC
LIMIT 10;
