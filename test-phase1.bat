@echo off
echo ========================================
echo 🧪 Testing CampusWorks Phase 1 Services
echo ========================================
echo.

echo ⏳ Waiting for services to start...
timeout /t 10 /nobreak >nul

echo.
echo 📱 Testing Eureka Server...
echo GET http://localhost:8761/actuator/health
curl -s http://localhost:8761/actuator/health
echo.

echo.
echo 🌐 Testing API Gateway...
echo GET http://localhost:8080/actuator/health
curl -s http://localhost:8080/actuator/health
echo.

echo.
echo 🔐 Testing Auth Service directly...
echo GET http://localhost:9001/auth/health
curl -s http://localhost:9001/auth/health
echo.

echo.
echo 🌐 Testing Auth Service through Gateway...
echo GET http://localhost:8080/api/auth/health
curl -s http://localhost:8080/api/auth/health
echo.

echo.
echo 📝 Testing User Registration...
echo POST http://localhost:8080/auth/register (Direct Path)
curl -X POST http://localhost:8080/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"student@campus.edu\",\"password\":\"student123\"}" ^
  -v

echo.
echo POST http://localhost:8080/api/auth/register (API Path)
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"student2@campus.edu\",\"password\":\"student123\"}" ^
  -v

echo.
echo.
echo 🔐 Testing User Login...
echo POST http://localhost:8080/auth/login (Direct Path)
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"student@campus.edu\",\"password\":\"student123\"}" ^
  -v

echo.
echo POST http://localhost:8080/api/auth/login (API Path)
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"student2@campus.edu\",\"password\":\"student123\"}" ^
  -v

echo.
echo.
echo 👑 Testing Admin Login...
echo POST http://localhost:8080/auth/login (Direct Path)
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@campusworks.com\",\"password\":\"admin123\"}" ^
  -v

echo.
echo POST http://localhost:8080/api/auth/login (API Path)
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@campusworks.com\",\"password\":\"admin123\"}" ^
  -v

echo.
echo ========================================
echo ✅ Phase 1 testing completed!
echo ========================================
echo.
echo 📋 Test Results Summary:
echo    - Eureka Server: Should show UP status
echo    - API Gateway: Should show UP status  
echo    - Auth Service: Should show "Phase 1 ✅"
echo    - Registration: Should create new STUDENT user
echo    - Login: Should return JWT token
echo    - Admin Login: Should work with default credentials
echo.
pause
