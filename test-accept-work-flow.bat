@echo off
echo Testing Accept Work Flow
echo ========================

echo.
echo 1. Starting all services...
call start-phase2.bat

echo.
echo 2. Waiting for services to start...
timeout /t 30

echo.
echo 3. Testing the flow:
echo    - Create a task
echo    - Accept a bid
echo    - Submit UPI ID (as bidder)
echo    - View UPI ID (as task owner)
echo    - Check if Accept Work button appears
echo    - Click Accept Work button
echo    - Verify task is completed

echo.
echo 4. Open browser and test:
echo    - Frontend: http://localhost:3000
echo    - Check browser console for debug logs
echo    - Look for "canAcceptWork check" logs

echo.
echo 5. Expected behavior:
echo    - After viewing UPI ID, Accept Work button should appear
echo    - Clicking Accept Work should complete the task
echo    - Task status should change to COMPLETED

echo.
echo Test completed. Check the browser console for any errors.
pause
