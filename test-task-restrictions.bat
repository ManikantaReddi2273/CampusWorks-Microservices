@echo off
echo ========================================
echo Test Task Edit/Delete Restrictions
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
echo Services Started for Testing!
echo ========================================
echo.
echo Test Scenarios:
echo.
echo 1. TASK WITH ACTIVE BIDDING (0 bids):
echo    - Edit/Delete buttons should be DISABLED
echo    - Tooltip: "Cannot edit or delete during bidding period"
echo    - Info alert should be visible
echo.
echo 2. TASK WITH BIDS AFTER DEADLINE:
echo    - Edit/Delete buttons should be DISABLED
echo    - Tooltip: "Cannot edit or delete - task received bids"
echo.
echo 3. TASK WITH NO BIDS AFTER DEADLINE:
echo    - Edit/Delete buttons should be ENABLED
echo    - Repost button should also be available
echo.
echo 4. TASK NOT OPEN (IN_PROGRESS, COMPLETED):
echo    - Only View button should be visible
echo.
echo Frontend: http://localhost:5173
echo API Gateway: http://localhost:8080
echo.
echo Check the My Tasks page to verify restrictions work correctly!
echo.
pause