@echo off
echo Fixing Task Deadlines...
echo.

mysql -u root -preddi2273 -e "USE campusworks_tasks; SOURCE fix-task-deadlines.sql;"

echo.
echo Task deadlines fixed!
pause
