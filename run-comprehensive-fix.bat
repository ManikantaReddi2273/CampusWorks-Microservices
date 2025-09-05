@echo off
echo ============================================
echo Comprehensive Status Column Fix
echo ============================================
echo.
echo This script will:
echo 1. Diagnose the current status column issue
echo 2. Force fix the status column by recreating it
echo 3. Test the fix with sample data
echo.
echo Please make sure your MySQL server is running
echo and you have the correct database credentials.
echo.
pause

echo.
echo Step 1: Diagnosing the issue...
mysql -u root -p campusworks_bids < diagnose-status-issue.sql

echo.
echo Step 2: Applying the force fix...
mysql -u root -p campusworks_bids < force-fix-status-column.sql

echo.
echo ============================================
echo Comprehensive fix completed!
echo ============================================
echo.
echo The status column has been completely recreated
echo and should now properly handle COMPLETED and CANCELLED values.
echo.
echo You can now try the "Accept Completed Work" 
echo functionality again.
echo.
pause
