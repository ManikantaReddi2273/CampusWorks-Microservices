@echo off
echo 🚀 Starting API Gateway with Simple CORS Configuration
echo Like Express.js: app.use(cors())
echo.

echo 🛑 Stopping any existing API Gateway...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Killing process %%a on port 8080
    taskkill /PID %%a /F >nul 2>&1
)

echo ⏳ Waiting for port cleanup...
timeout /t 2 /nobreak >nul

echo 🔧 Starting API Gateway...
cd /d "%~dp0\api-gateway"
start "API Gateway - CORS Enabled" cmd /k "mvn spring-boot:run"

echo.
echo ✅ API Gateway starting with simple CORS configuration!
echo 🌐 URL: http://localhost:8080
echo 🔧 CORS enabled for: http://localhost:3000
echo.
echo Check the new window for startup logs...
pause
