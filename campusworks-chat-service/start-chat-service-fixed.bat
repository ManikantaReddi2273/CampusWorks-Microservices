@echo off
echo ========================================
echo Starting CampusWorks Chat Service
echo ========================================

REM Check if Node.js is installed
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

REM Check if MongoDB is running
echo Checking MongoDB connection...
node -e "const mongoose = require('mongoose'); mongoose.connect('mongodb://localhost:27017/campusworks_chat').then(() => { console.log('MongoDB connected'); process.exit(0); }).catch(err => { console.log('MongoDB connection failed:', err.message); process.exit(1); });" 2>nul
if %errorlevel% neq 0 (
    echo WARNING: MongoDB connection failed
    echo Please make sure MongoDB is running on localhost:27017
    echo You can start MongoDB with: mongod
    echo.
    echo Continuing anyway - the service will try to connect...
)

REM Check if Spring Boot services are running
echo Checking Spring Boot services...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Spring Boot services not detected
    echo Please make sure the following services are running:
    echo - Eureka Server: http://localhost:8761
    echo - API Gateway: http://localhost:8080
    echo - Auth Service: http://localhost:9000
    echo - Task Service: http://localhost:9001
    echo - Bidding Service: http://localhost:9002
    echo - Profile Service: http://localhost:9003
    echo.
    echo The chat service will work with limited functionality...
)

REM Install dependencies if needed
if not exist node_modules (
    echo Installing dependencies...
    npm install
    if %errorlevel% neq 0 (
        echo ERROR: Failed to install dependencies
        pause
        exit /b 1
    )
)

REM Set environment variables
set NODE_ENV=development
set PORT=3001
set SPRING_BOOT_BASE_URL=http://localhost:8080
set AUTH_SERVICE_URL=http://localhost:9000
set TASK_SERVICE_URL=http://localhost:9001
set BIDDING_SERVICE_URL=http://localhost:9002
set JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
set MONGODB_URI=mongodb://localhost:27017/campusworks_chat
set SOCKET_CORS_ORIGIN=http://localhost:3000

echo.
echo Starting chat service...
echo Port: 3001
echo MongoDB: mongodb://localhost:27017/campusworks_chat
echo Spring Boot: http://localhost:8080
echo CORS Origin: http://localhost:3000
echo.

REM Start the service
node src/server.js

REM If the service exits, show error
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Chat service exited with error code %errorlevel%
    echo Check the logs above for details
    pause
)
