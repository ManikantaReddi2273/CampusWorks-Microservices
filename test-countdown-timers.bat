@echo off
echo ========================================
echo Test Live Countdown Timers
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
echo Test the Live Countdown Timers:
echo.
echo 1. BROWSE TASKS (Tasks page):
echo    - Go to Tasks page to see all available tasks
echo    - Each task card shows "Time Remaining" with live countdown
echo    - Format: "X days : Y hours : Z min : W sec"
echo    - Color changes based on urgency (Green/Yellow/Orange/Red)
echo    - Updates every second in real-time
echo.
echo 2. TASK CARDS (My Tasks page):
echo    - Create a task with bidding deadline
echo    - Check "Time Remaining" section shows live countdown
echo    - Format: "X days : Y hours : Z min : W sec"
echo    - Color changes based on urgency:
echo      * Green: More than 24 hours
echo      * Yellow: 6-24 hours remaining
echo      * Orange: 1-6 hours remaining
echo      * Red: Less than 1 hour (blinks)
echo      * Red: Expired
echo.
echo 3. ACCEPTED BID CARDS (My Bids page):
echo    - Place a bid on a task
echo    - Accept the bid as task owner
echo    - Check "Time Remaining" section shows live countdown
echo    - Same color coding as task cards
echo    - Only shows for accepted bids with valid deadline
echo.
echo 3. COUNTDOWN FEATURES:
echo    - Updates every second in real-time
echo    - Shows "Expired" when deadline passes
echo    - Blinking animation for critical time (1 hour or less)
echo    - Compact chip-style display
echo    - Responsive design
echo.
echo Frontend: http://localhost:5173
echo API Gateway: http://localhost:8080
echo.
echo Watch the countdown timers update in real-time!
echo.
pause
