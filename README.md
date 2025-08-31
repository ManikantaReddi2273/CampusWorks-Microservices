# 💰 CampusWorks Payment Gateway Service

## 🎯 Overview

The Payment Gateway Service is a comprehensive financial management microservice that handles all payment operations, escrow management, and financial transactions for the CampusWorks platform. It integrates with **Razorpay** for secure payment processing and implements **custom business logic** where escrow expiration is tied directly to task deadlines for optimal user experience.

This service was implemented based on your specific requirements to eliminate the traditional 30-day escrow period and instead tie all escrow operations directly to the task completion deadlines set by clients.

## 🔥 Key Features

### ✅ **Your Improved Business Logic Implemented**
- **Escrow tied to task deadline** (not fixed 30 days)
- **Immediate payment release** on work acceptance
- **Immediate refund** on work rejection
- **Automatic refund** when task deadline expires

### 🏦 **Core Functionality**
- **Payment Processing** with Razorpay integration
- **Escrow Management** with task deadline expiration
- **Wallet System** for users (balance, earnings, spending tracking)
- **Transaction History** with detailed records
- **Webhook Processing** for real-time payment updates
- **Scheduled Tasks** for automatic deadline checking

## 🏗️ Architecture

### 📊 Database Schema
```
payments (payment records)
├── escrows (money held until work completion)
├── transactions (all financial movements)
├── wallets (user balances and statistics)
└── razorpay_webhooks (webhook processing logs)
```

### 🔗 Service Communication
```
Payment Service communicates with:
├── Task Service (get task details, update status)
├── Profile Service (update earnings, get profile info)
├── Bidding Service (get winning bid, notify payment status)
└── Razorpay (payment processing, webhooks)
```

## 🚀 Quick Start

### 1. **Database Setup**
```bash
mysql -u root -p < setup-payment-database.sql
```

### 2. **Configure Razorpay Keys**
Edit `src/main/resources/application.properties`:
```properties
# 🔑 REPLACE THESE WITH YOUR ACTUAL RAZORPAY KEYS
payment.razorpay.key-id=rzp_test_YOUR_KEY_ID_HERE
payment.razorpay.key-secret=YOUR_SECRET_KEY_HERE
payment.razorpay.webhook-secret=YOUR_WEBHOOK_SECRET_HERE
```

### 3. **Start the Service**
```bash
mvn spring-boot:run
```

Service will start on **http://localhost:8084**

## 🔑 Razorpay Integration Setup

### Step 1: Create Razorpay Account
1. Go to [razorpay.com](https://razorpay.com)
2. Sign up as Developer/Startup
3. Fill business details (use "CampusWorks" as business name)

### Step 2: Get API Keys
1. Login to Razorpay Dashboard
2. Go to Settings → API Keys
3. Copy your **Test Keys**:
   - Key ID: `rzp_test_xxxxxxxxxx`
   - Key Secret: `xxxxxxxxxxxxxxxxxx`

### Step 3: Configure Webhooks
1. Go to Settings → Webhooks
2. Add webhook URL: `http://your-domain.com/api/payments/webhooks/razorpay`
3. Select events: `payment.captured`, `payment.failed`, `order.paid`, `refund.created`
4. Copy webhook secret

### Step 4: Update Configuration
```properties
payment.razorpay.key-id=rzp_test_YOUR_ACTUAL_KEY_ID
payment.razorpay.key-secret=YOUR_ACTUAL_SECRET_KEY
payment.razorpay.webhook-secret=YOUR_ACTUAL_WEBHOOK_SECRET
payment.razorpay.mode=test
```

## 📋 API Endpoints

### 💰 **Payment Operations**
```
POST   /api/payments/tasks/{taskId}/create     # Create payment for winning bid
POST   /api/payments/tasks/{taskId}/accept     # Accept work & release payment
POST   /api/payments/tasks/{taskId}/reject     # Reject work & refund payment
GET    /api/payments/tasks/{taskId}            # Get payment details
```

### 👛 **Wallet Operations**
```
GET    /api/payments/wallet                    # Get user wallet
GET    /api/payments/transactions              # Get transaction history
```

### 📊 **Escrow Operations**
```
GET    /api/payments/escrows/task/{taskId}     # Get escrow details
```

### 🔔 **Webhooks**
```
POST   /api/payments/webhooks/razorpay         # Razorpay webhook handler
GET    /api/payments/webhooks/status           # Webhook processing status
```

### 🔧 **Admin Operations**
```
POST   /api/payments/admin/trigger-deadline-check  # Manual deadline check
GET    /api/payments/admin/config                   # Get scheduler config
```

## 🔄 Payment Flow

### 1. **Payment Creation**
```
Client → POST /api/payments/tasks/123/create
├── Get task details (including deadline)
├── Get winning bid details
├── Calculate platform fee (5%)
├── Create payment record
├── Create Razorpay order
├── Create escrow (expires at task deadline) 🔥
└── Return payment details for frontend
```

### 2. **Payment Processing**
```
User pays via Razorpay → Webhook received
├── Verify signature
├── Update payment status
├── Fund escrow
├── Update wallet spending
└── Notify bidding service
```

### 3. **Work Acceptance** (Your Logic ✅)
```
Client → POST /api/payments/tasks/123/accept
├── Validate escrow can be released
├── Release payment to worker immediately 🔥
├── Add earnings to worker wallet
├── Update task status to COMPLETED
├── Update profile earnings
└── Create transaction records
```

### 4. **Work Rejection** (Your Logic ✅)
```
Client → POST /api/payments/tasks/123/reject
├── Validate escrow can be refunded
├── Refund payment to client immediately 🔥
├── Add refund to client wallet
├── Reopen task for new bids
└── Create transaction records
```

### 5. **Automatic Expiration** (Your Logic ✅)
```
Scheduled Task (every hour) → Check expired deadlines
├── Find escrows with expired task deadlines
├── Auto-refund to client 🔥
├── Mark task as failed and reopen
└── Create transaction records
```

## 🔧 Configuration

### Business Logic Settings
```properties
# Platform fee (5%)
payment.platform-fee-percentage=0.05

# Transaction limits
payment.min-transaction-amount=10.00
payment.max-transaction-amount=100000.00

# Escrow settings
payment.escrow.auto-check-interval=3600000  # 1 hour
payment.escrow.notification-enabled=true
```

### Database Settings
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campusworks_payment
spring.datasource.username=root
spring.datasource.password=root
```

## 📊 Monitoring & Health

### Health Check
```
GET /actuator/health
```

### Webhook Status
```
GET /api/payments/webhooks/status
{
  "total": 150,
  "processed": 148,
  "failed": 1,
  "pending": 1,
  "success_rate": 98.67
}
```

### Scheduler Status
```
GET /api/payments/admin/config
{
  "configuration": "Auto-check interval: 3600000 ms (1 hours), Notifications: true"
}
```

## 🧪 Testing

### Test with Razorpay Test Cards
```
Success Card: 4111 1111 1111 1111
Failure Card: 4111 1111 1111 1112
CVV: Any 3 digits
Expiry: Any future date
```

### Test UPI
```
Success UPI: success@razorpay
```

## 🔒 Security Features

- **JWT Authentication** via API Gateway headers
- **Webhook signature verification** for Razorpay
- **User authorization** checks for all operations
- **Transaction logging** for audit trails
- **Secure fallback** behavior when services are unavailable

## 🎯 Business Logic Highlights

### ✅ **Your Improvements Implemented:**

1. **Escrow Expiration = Task Deadline**
   - No more fixed 30-day periods
   - Escrow expires exactly when task deadline expires
   - More predictable for both clients and workers

2. **Immediate Actions**
   - Accept work → Money released immediately
   - Reject work → Money refunded immediately
   - No waiting periods or delays

3. **Automatic Handling**
   - Task deadline expires → Auto-refund to client
   - Task reopened for new bids automatically
   - No manual intervention required

## 📈 Next Steps

1. **Testing**: Test all payment flows with Razorpay test environment
2. **Notification Service**: Integrate for payment notifications
3. **Frontend Integration**: Connect with React frontend
4. **Production**: Switch to live Razorpay keys for production

---

**🎉 Payment Service is ready with your improved escrow logic! The system now handles payments exactly as you specified - tied to task deadlines with immediate actions on acceptance/rejection.**
