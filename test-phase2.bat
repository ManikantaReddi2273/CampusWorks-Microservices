@echo off
echo ============================================
echo CampusWorks Phase 2 - Testing All Services
echo ============================================
echo.

echo Waiting for services to be ready...
timeout /t 30 /nobreak > nul

echo.
echo ============================================
echo 1. Testing Health Checks
echo ============================================
echo.

echo Testing Eureka Server...
curl -s http://localhost:8761/actuator/health
echo.

echo Testing API Gateway...
curl -s http://localhost:8080/actuator/health
echo.

echo Testing Auth Service...
curl -s http://localhost:9000/actuator/health
echo.

echo Testing Task Service...
curl -s http://localhost:9001/actuator/health
echo.

echo Testing Bidding Service...
curl -s http://localhost:9002/actuator/health
echo.

echo Testing Profile Service...
curl -s http://localhost:9003/actuator/health
echo.

echo.
echo ============================================
echo 2. Testing Authentication Flow
echo ============================================
echo.

echo Registering a new student user...
curl -X POST http://localhost:8080/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"student@campusworks.com\",\"password\":\"student123\"}"
echo.

echo Waiting for registration to complete...
timeout /t 3 /nobreak > nul

echo Logging in as student...
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"student@campusworks.com\",\"password\":\"student123\"}"
echo.

echo.
echo ============================================
echo 3. Testing Task Service (with JWT)
echo ============================================
echo.

echo Note: Replace JWT_TOKEN with actual token from login response
echo.

echo Creating a new task...
curl -X POST http://localhost:8080/tasks ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer JWT_TOKEN" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: student@campusworks.com" ^
  -d "{\"title\":\"Math Assignment Help\",\"description\":\"Need help with calculus problems\",\"budget\":50.00,\"category\":\"MATHEMATICS\",\"biddingDeadline\":\"2024-12-31T23:59:59\",\"completionDeadline\":\"2025-01-07T23:59:59\"}"
echo.

echo Getting all tasks...
curl -s http://localhost:8080/tasks
echo.

echo.
echo ============================================
echo 4. Testing Bidding Service (with JWT)
echo ============================================
echo.

echo Placing a bid on task...
curl -X POST http://localhost:8080/bids ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer JWT_TOKEN" ^
  -H "X-User-Id: 2" ^
  -H "X-User-Email: worker@campusworks.com" ^
  -d "{\"taskId\":1,\"amount\":45.00,\"proposal\":\"I can help with calculus problems\"}"
echo.

echo Getting bids for task...
curl -s http://localhost:8080/bids/task/1
echo.

echo.
echo ============================================
echo 5. Testing Profile Service (with JWT)
echo ============================================
echo.

echo Creating a user profile...
curl -X POST http://localhost:8080/profiles ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer JWT_TOKEN" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: student@campusworks.com" ^
  -d "{\"firstName\":\"John\",\"lastName\":\"Student\",\"university\":\"Tech University\",\"major\":\"Computer Science\",\"academicYear\":\"3rd Year\",\"bio\":\"Computer Science student looking for opportunities\"}"
echo.

echo Getting all public profiles...
curl -s http://localhost:8080/profiles/public
echo.

echo.
echo ============================================
echo 6. Testing API Gateway Routes
echo ============================================
echo.

echo Testing Task Service through Gateway...
curl -s http://localhost:8080/api/tasks
echo.

echo Testing Bidding Service through Gateway...
curl -s http://localhost:8080/api/bids
echo.

echo Testing Profile Service through Gateway...
curl -s http://localhost:8080/api/profiles
echo.

echo.
echo ============================================
echo Testing Complete!
echo ============================================
echo.
echo Check the responses above for any errors.
echo If you see JWT_TOKEN in responses, replace it with actual token.
echo.
pause
