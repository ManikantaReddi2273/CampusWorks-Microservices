@echo off
echo ========================================
echo 🚀 Starting CampusWorks Phase 1 Services
echo ========================================
echo.

echo 📱 Starting Eureka Server (Port: 8761)...
start "Eureka Server" cmd /k "cd eureka-server && mvn spring-boot:run"
timeout /t 15 /nobreak >nul

echo 🌐 Starting API Gateway (Port: 8080)...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
timeout /t 15 /nobreak >nul

echo 🔐 Starting Auth Service (Port: 9001)...
start "Auth Service" cmd /k "cd auth-service && mvn spring-boot:run"
timeout /t 15 /nobreak >nul

echo.
echo ========================================
echo ✅ All Phase 1 services are starting...
echo ========================================
echo.
echo 📍 Service URLs:
echo    - Eureka Dashboard: http://localhost:8761
echo    - API Gateway:      http://localhost:8080
echo    - Auth Service:     http://localhost:9001
echo    - Gateway Auth:     http://localhost:8080/auth (Direct)
echo    - Gateway Auth:     http://localhost:8080/api/auth (API Style)
echo.
echo ⏳ Wait for all services to start before testing...
echo 🧪 Run 'test-phase1.bat' to test the services
echo.
pause
