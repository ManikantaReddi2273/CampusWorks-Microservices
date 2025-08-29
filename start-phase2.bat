@echo off
echo ============================================
echo CampusWorks Phase 2 - Starting All Services
echo ============================================
echo.

echo Starting Eureka Server...
start "Eureka Server" cmd /k "cd eureka-server && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo Starting API Gateway...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo Starting Auth Service...
start "Auth Service" cmd /k "cd auth-service && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo Starting Task Service...
start "Task Service" cmd /k "cd task-service && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo Starting Bidding Service...
start "Bidding Service" cmd /k "cd bidding-service && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo Starting Profile Service...
start "Profile Service" cmd /k "cd profile-service && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo.
echo ============================================
echo All services are starting up...
echo ============================================
echo.
echo Service URLs:
echo - Eureka Server: http://localhost:8761
echo - API Gateway: http://localhost:8080
echo - Auth Service: http://localhost:9000
echo - Task Service: http://localhost:9001
echo - Bidding Service: http://localhost:9002
echo - Profile Service: http://localhost:9003
echo.
echo Wait for all services to start up completely.
echo Check Eureka Dashboard to verify all services are registered.
echo.
pause
