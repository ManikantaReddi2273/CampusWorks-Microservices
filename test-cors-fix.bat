@echo off
echo Testing CORS Fix
echo =================

echo.
echo 1. Starting all services...
call start-phase2.bat

echo.
echo 2. Waiting for services to start...
timeout /t 30

echo.
echo 3. Testing CORS configuration:
echo    - API Gateway: http://localhost:8080
echo    - Frontend: http://localhost:3000
echo    - Check browser console for CORS errors

echo.
echo 4. Expected behavior:
echo    - NO CORS errors in browser console
echo    - Single Access-Control-Allow-Origin header
echo    - All API calls work without CORS issues

echo.
echo 5. Test endpoints:
echo    - Login: POST http://localhost:8080/api/auth/login
echo    - Tasks: GET http://localhost:8080/api/tasks
echo    - Bids: GET http://localhost:8080/api/bids

echo.
echo 6. Check browser Network tab:
echo    - Look for OPTIONS preflight requests
echo    - Verify single CORS header in responses
echo    - No duplicate Access-Control-Allow-Origin headers

echo.
echo CORS fix test completed!
echo Check browser console and Network tab for any remaining CORS errors.
pause
