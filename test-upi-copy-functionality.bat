@echo off
echo ========================================
echo Test UPI ID Copy Functionality
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
echo Test the UPI Copy Functionality:
echo.
echo 1. Create a task and place a bid
echo 2. Accept the bid as task owner
echo 3. As bidder, submit UPI ID for completed task
echo 4. As task owner, click "View UPI ID" button
echo 5. In the UPI ID modal:
echo    - UPI ID should be displayed in a highlighted box
echo    - Copy button (📋) should be visible next to UPI ID
echo    - Click copy button - should show checkmark (✓) briefly
echo    - Tooltip should show "Copied!" when clicked
echo    - UPI ID should be copied to clipboard
echo.
echo Frontend: http://localhost:5173
echo API Gateway: http://localhost:8080
echo.
echo Test the copy functionality by pasting the UPI ID somewhere!
echo.
pause
