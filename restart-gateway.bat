@echo off
echo 🔄 Restarting API Gateway to fix CORS issue...
echo.

echo 🛑 Stopping any running API Gateway processes...
for /f "tokens=2" %%i in ('netstat -ano ^| findstr :8080') do (
    echo Killing process on port 8080: %%i
    taskkill /PID %%i /F >nul 2>&1
)

echo.
echo ⏳ Waiting 3 seconds for cleanup...
timeout /t 3 /nobreak >nul

echo.
echo 🚀 Starting API Gateway with CORS fix...
cd api-gateway
start "API Gateway" mvn spring-boot:run

echo.
echo ✅ API Gateway restart initiated!
echo 🌐 Gateway URL: http://localhost:8080
echo 🔧 CORS issue should now be fixed
echo.
echo Press any key to close this window...
pause >nul
