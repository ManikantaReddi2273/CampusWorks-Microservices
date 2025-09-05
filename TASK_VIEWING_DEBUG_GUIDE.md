# Task Viewing Debug Guide

## 🐛 **Issue**: "Failed to load task details. Please try again."

### **Debugging Steps Added:**

1. **Enhanced Error Handling in TaskDetailPage**:
   - Added detailed console logging
   - Added specific error messages for different scenarios
   - Added task ID validation

2. **Enhanced Debugging in MyBidsPage**:
   - Added console logging for bid data
   - Added taskId validation before navigation
   - Added disabled state for View Task button when taskId is missing

3. **Debug Script**: `debug-task-viewing.bat`
   - Starts all services for testing
   - Provides step-by-step debugging instructions

### **Potential Issues & Solutions:**

#### **Issue 1: Missing taskId in Bid Data**
**Symptoms**: View Task button is disabled, console shows "Task ID not available"
**Solution**: Check if bid data includes taskId field

#### **Issue 2: Invalid taskId Format**
**Symptoms**: Error shows "Invalid task ID format"
**Solution**: Check if taskId is a valid number

#### **Issue 3: Task Not Found (404)**
**Symptoms**: Error shows "Task not found"
**Solution**: Check if task exists in database

#### **Issue 4: Permission Issues (403)**
**Symptoms**: Error shows "You do not have permission to view this task"
**Solution**: Check user authentication and task ownership

#### **Issue 5: Server Error (500)**
**Symptoms**: Error shows "Server error"
**Solution**: Check backend service logs

### **Debugging Process:**

1. **Open Browser Console** (F12)
2. **Navigate to My Bids page**
3. **Check Console Logs**:
   ```javascript
   // Look for these logs:
   Bids response: {...}
   Bids data: [...]
   Processed bids: [...]
   Bid 0: {id: 1, taskId: 123, status: "REJECTED", amount: 400}
   ```

4. **Click View Task Button**:
   ```javascript
   // Look for these logs:
   Viewing task with ID: 123
   Fetching task details for ID: 123
   Task details response: {...}
   ```

5. **Check for Errors**:
   - Invalid task ID format
   - Missing task ID
   - API call failures
   - Network errors

### **Quick Fixes to Try:**

#### **Fix 1: Check API Response Structure**
```javascript
// In MyBidsPage.jsx, check if response structure is correct
const response = await apiService.bids.getByUserEmail(user.email);
console.log('Full response:', response);
console.log('Response data:', response.data);
```

#### **Fix 2: Verify Task ID in Bid Object**
```javascript
// Check if bid.taskId exists and is valid
bidsData.forEach((bid, index) => {
  console.log(`Bid ${index}:`, {
    id: bid.id,
    taskId: bid.taskId,
    hasTaskId: !!bid.taskId,
    taskIdType: typeof bid.taskId
  });
});
```

#### **Fix 3: Test Direct API Call**
```javascript
// Test if task API works directly
const testTaskId = 1; // Replace with actual task ID
const response = await apiService.tasks.getById(testTaskId);
console.log('Direct task API test:', response);
```

### **Backend Verification:**

1. **Check Task Service Logs**:
   - Look for "Retrieving task with ID: X"
   - Check for any exceptions

2. **Check Bidding Service Logs**:
   - Look for "Retrieving all bids by user email"
   - Verify bid data includes taskId

3. **Check API Gateway Logs**:
   - Look for routing issues
   - Check for authentication problems

### **Common Solutions:**

1. **If taskId is missing**: Check bid repository query
2. **If taskId is null**: Check database data integrity
3. **If API call fails**: Check service connectivity
4. **If 404 error**: Check if task exists
5. **If 403 error**: Check user permissions

### **Test Commands:**

```bash
# Test task API directly
curl -X GET "http://localhost:8080/api/tasks/1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test bids API
curl -X GET "http://localhost:8080/api/bids/user/email/user@example.com" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### **Next Steps:**

1. Run the debug script
2. Check console logs
3. Identify the specific error
4. Apply the appropriate fix
5. Test the solution

The enhanced error handling and logging should help identify the exact cause of the "Failed to load task details" error.
