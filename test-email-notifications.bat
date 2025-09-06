@echo off
echo ========================================
echo Testing Email Notifications for CampusWorks
echo ========================================
echo.

echo Starting Eureka Server...
start "Eureka Server" cmd /k "cd eureka-server && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo Starting API Gateway...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo Starting Auth Service...
start "Auth Service" cmd /k "cd auth-service && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo Starting Task Service...
start "Task Service" cmd /k "cd task-service && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo Starting Bidding Service...
start "Bidding Service" cmd /k "cd bidding-service && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo Starting Profile Service...
start "Profile Service" cmd /k "cd profile-service && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo Starting Frontend...
start "Frontend" cmd /k "cd campus-works-frontend && npm run dev"
timeout /t 10 /nobreak > nul

echo.
echo ========================================
echo All services started!
echo ========================================
echo.
echo Services running on:
echo - Eureka Server: http://localhost:8761
echo - API Gateway: http://localhost:8080
echo - Auth Service: http://localhost:9000
echo - Task Service: http://localhost:9001
echo - Bidding Service: http://localhost:9002
echo - Profile Service: http://localhost:9003
echo - Frontend: http://localhost:5173
echo.
echo ========================================
echo Email Notification Test Instructions:
echo ========================================
echo.
echo 1. Open http://localhost:5173 in your browser
echo 2. Register a new account or login
echo 3. Create a new task
echo 4. Place a bid on the task
echo 5. Wait for the bidding deadline to expire (1 hour)
echo 6. Check your email for notifications!
echo.
echo Email notifications will be sent for:
echo - Task assignment (bidder and owner)
echo - UPI ID submission (task owner)
echo - Work acceptance (bidder)
echo.
echo Press any key to stop all services...
pause > nul

echo Stopping all services...
taskkill /f /im java.exe > nul 2>&1
taskkill /f /im node.exe > nul 2>&1
echo All services stopped.
