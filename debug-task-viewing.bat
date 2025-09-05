@echo off
echo ========================================
echo Debug Task Viewing Issue
echo ========================================
echo.

echo 1. Starting Eureka Server...
cd eureka-server
start "Eureka Server" cmd /k "mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo 2. Starting Task Service...
cd ..\task-service
start "Task Service" cmd /k "mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo 3. Starting Bidding Service...
cd ..\bidding-service
start "Bidding Service" cmd /k "mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo 4. Starting API Gateway...
cd ..\api-gateway
start "API Gateway" cmd /k "mvn spring-boot:run"
timeout /t 5 /nobreak > nul

echo 5. Starting Frontend...
cd ..\campus-works-frontend
start "Frontend" cmd /k "npm run dev"
timeout /t 5 /nobreak > nul

echo.
echo ========================================
echo Services Started for Debugging!
echo ========================================
echo.
echo Debug Steps:
echo 1. Open browser console (F12)
echo 2. Go to My Bids page
echo 3. Check console logs for bid data
echo 4. Look for taskId values in the logs
echo 5. Try clicking View Task button
echo 6. Check console for error messages
echo.
echo Frontend: http://localhost:5173
echo API Gateway: http://localhost:8080
echo.
echo Check the console logs to see:
echo - What bid data is being received
echo - Whether taskId is present in bid objects
echo - What error occurs when viewing task details
echo.
pause
