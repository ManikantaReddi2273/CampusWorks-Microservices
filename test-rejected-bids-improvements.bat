@echo off
echo ========================================
echo Testing Rejected Bids Improvements
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
echo Services Started Successfully!
echo ========================================
echo.
echo Test Scenarios for Rejected Bids:
echo 1. Create a task as owner
echo 2. Place a bid on the task as another user
echo 3. Reject the bid as task owner with a reason
echo 4. Check My Bids page - should show rejection reason
echo 5. Test View Task button - should work properly
echo 6. Test Delete button - should remove rejected bid
echo.
echo Frontend: http://localhost:5173
echo API Gateway: http://localhost:8080
echo.
pause
