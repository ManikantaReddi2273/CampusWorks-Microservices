@echo off
echo ============================================
echo Testing Timestamp Synchronization
echo ============================================
echo.
echo This script will test the complete flow:
echo 1. Create a task
echo 2. Place a bid
echo 3. Accept the bid (should sync accepted_at)
echo 4. Submit UPI ID
echo 5. View UPI ID
echo 6. Accept completed work (should sync completed_at)
echo.
echo Please make sure all services are running:
echo - Eureka Server (port 8761)
echo - API Gateway (port 8080)
echo - Auth Service (port 9000)
echo - Task Service (port 9001)
echo - Bidding Service (port 9002)
echo - Profile Service (port 9003)
echo - Payment Service (port 8084)
echo.
pause

echo.
echo Starting timestamp synchronization test...
echo.

REM Test 1: Create a task
echo Step 1: Creating a test task...
curl -X POST "http://localhost:8080/api/tasks" ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: test@example.com" ^
  -H "X-User-Roles: USER" ^
  -d "{\"title\":\"Test Task for Sync\",\"description\":\"Testing timestamp synchronization\",\"budget\":100.00,\"category\":\"PROGRAMMING\",\"completionDeadline\":\"2024-12-31T23:59:59\"}" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 2: Placing a bid on the task...
curl -X POST "http://localhost:8080/api/bids" ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 2" ^
  -H "X-User-Email: bidder@example.com" ^
  -H "X-User-Roles: USER" ^
  -d "{\"taskId\":1,\"amount\":80.00,\"proposal\":\"I can complete this task efficiently\"}" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 3: Accepting the bid (should sync accepted_at)...
curl -X POST "http://localhost:8080/api/bids/1/accept" ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: test@example.com" ^
  -H "X-User-Roles: USER" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 4: Checking task status after bid acceptance...
curl -X GET "http://localhost:8080/api/tasks/1" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: test@example.com" ^
  -H "X-User-Roles: USER" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 5: Submitting UPI ID...
curl -X POST "http://localhost:8080/api/bids/1/submit-upi" ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 2" ^
  -H "X-User-Email: bidder@example.com" ^
  -H "X-User-Roles: USER" ^
  -d "{\"upiId\":\"bidder@paytm\"}" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 6: Viewing UPI ID...
curl -X POST "http://localhost:8080/api/bids/1/view-upi" ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: test@example.com" ^
  -H "X-User-Roles: USER" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 7: Accepting completed work (should sync completed_at)...
curl -X POST "http://localhost:8080/api/bids/1/accept-work" ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: test@example.com" ^
  -H "X-User-Roles: USER" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 8: Final task status check...
curl -X GET "http://localhost:8080/api/tasks/1" ^
  -H "X-User-Id: 1" ^
  -H "X-User-Email: test@example.com" ^
  -H "X-User-Roles: USER" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo Step 9: Final bid status check...
curl -X GET "http://localhost:8080/api/bids/1" ^
  -H "X-User-Id: 2" ^
  -H "X-User-Email: bidder@example.com" ^
  -H "X-User-Roles: USER" ^
  -w "\nHTTP Status: %%{http_code}\n\n"

echo.
echo ============================================
echo Timestamp Synchronization Test Complete!
echo ============================================
echo.
echo Check the responses above to verify:
echo 1. Task acceptedAt should be set when bid is accepted
echo 2. Task completedAt should be set when work is accepted
echo 3. Bid acceptedAt should match task acceptedAt
echo 4. Both timestamps should be properly formatted
echo.
pause
