@echo off
echo ============================================
echo Fixing Bids Table Status Column Issue
echo ============================================
echo.
echo This script will fix the status column size issue
echo that prevents COMPLETED and CANCELLED status values
echo from being stored in the bids table.
echo.
echo Please make sure your MySQL server is running
echo and you have the correct database credentials.
echo.
pause

echo.
echo Running the status column fix...
mysql -u root -p campusworks_bids < fix-bids-status-column.sql

echo.
echo ============================================
echo Status column fix completed!
echo ============================================
echo.
echo You can now try the "Accept Completed Work" 
echo functionality again.
echo.
pause
