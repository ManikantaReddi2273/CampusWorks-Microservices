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

### 📁 **Service Architecture**
```
payment-service/
├── src/main/java/com/campusworks/payment/
│   ├── entity/          # JPA Entities (Payment, Escrow, Transaction, Wallet, etc.)
│   ├── repository/      # JPA Repositories for database operations
│   ├── service/         # Business logic (PaymentService, RazorpayService, etc.)
│   ├── controller/      # REST Controllers (PaymentController, WebhookController)
│   ├── client/          # Feign Clients for inter-service communication
│   ├── dto/             # Data Transfer Objects
│   └── config/          # Configuration classes
└── src/main/resources/
    └── application.properties  # Service configuration
```

## 🗄️ **Database Entities Implemented**

### **Payment Entity**
```java
@Entity
@Table(name = "payments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @Column(name = "client_user_id", nullable = false)
    private Long clientUserId;
    
    @Column(name = "worker_user_id", nullable = false)
    private Long workerUserId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;
    
    @Column(name = "worker_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal workerAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;
    
    @Column(name = "razorpay_payment_id", unique = true)
    private String razorpayPaymentId;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";
    
    // ... timestamps and other fields
}
```

### **Escrow Entity (Your Custom Logic)**
```java
@Entity
@Table(name = "escrows")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Escrow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false, unique = true)
    private Long taskId;
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @Column(name = "client_user_id", nullable = false)
    private Long clientUserId;
    
    @Column(name = "worker_user_id", nullable = false)
    private Long workerUserId;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;
    
    @Column(name = "worker_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal workerAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EscrowStatus status;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;  // 🔥 TIED TO TASK DEADLINE
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Payment payment;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean canBeReleased() {
        return status == EscrowStatus.FUNDED;
    }
    
    public boolean canBeRefunded() {
        return status == EscrowStatus.FUNDED || status == EscrowStatus.CREATED;
    }
}
```

### **Transaction Entity**
```java
@Entity
@Table(name = "transactions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "task_id")
    private Long taskId;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "escrow_id")
    private Long escrowId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "balance_before", precision = 10, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;
}
```

### **Wallet Entity**
```java
@Entity
@Table(name = "wallets")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "total_earned", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalEarned = BigDecimal.ZERO;
    
    @Column(name = "total_spent", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Column(name = "total_refunded", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunded = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;
    
    public void addEarnings(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalEarned = this.totalEarned.add(amount);
            addBalance(amount);
        }
    }
    
    public void addSpending(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalSpent = this.totalSpent.add(amount);
        }
    }
    
    public void addRefund(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalRefunded = this.totalRefunded.add(amount);
            addBalance(amount);
        }
    }
}
```

## 🔄 **Inter-Service Communication**

### **Feign Clients with Circuit Breakers**
```java
@FeignClient(name = "task-service", fallback = TaskServiceClientFallback.class)
public interface TaskServiceClient {
    @GetMapping("/tasks/{taskId}")
    TaskResponse getTask(@PathVariable Long taskId, 
                        @RequestHeader("X-User-Id") String userId,
                        @RequestHeader("X-User-Email") String userEmail,
                        @RequestHeader("X-User-Roles") String userRoles);
    
    @PutMapping("/tasks/{taskId}/status")
    void updateTaskStatus(@PathVariable Long taskId, 
                         @RequestBody TaskStatusUpdateRequest request,
                         @RequestHeader("X-User-Id") String userId,
                         @RequestHeader("X-User-Email") String userEmail,
                         @RequestHeader("X-User-Roles") String userRoles);
}

@FeignClient(name = "profile-service", fallback = ProfileServiceClientFallback.class)
public interface ProfileServiceClient {
    @PutMapping("/users/{userId}/earnings")
    void updateUserEarnings(@PathVariable Long userId, 
                           @RequestBody AddEarningsRequest request,
                           @RequestHeader("X-User-Id") String requesterId,
                           @RequestHeader("X-User-Email") String requesterEmail,
                           @RequestHeader("X-User-Roles") String requesterRoles);
}

@FeignClient(name = "bidding-service", fallback = BiddingServiceClientFallback.class)
public interface BiddingServiceClient {
    @GetMapping("/bids/tasks/{taskId}/winner")
    BidResponse getWinningBid(@PathVariable Long taskId,
                             @RequestHeader("X-User-Id") String userId,
                             @RequestHeader("X-User-Email") String userEmail,
                             @RequestHeader("X-User-Roles") String userRoles);
}
```

### **Header Propagation Configuration**
```java
@Configuration
@Slf4j
public class FeignClientConfig {
    @Bean
    public RequestInterceptor authHeaderInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String userId = request.getHeader("X-User-Id");
                    String userEmail = request.getHeader("X-User-Email");
                    String userRoles = request.getHeader("X-User-Roles");
                    
                    if (userId != null) template.header("X-User-Id", userId);
                    if (userEmail != null) template.header("X-User-Email", userEmail);
                    if (userRoles != null) template.header("X-User-Roles", userRoles);
                }
            }
        };
    }
}
```

## 🚀 Quick Start

### 1. **Database Setup**
```bash
mysql -u root -p < setup-payment-database.sql
```

### 2. **Configure Razorpay Keys**
Edit `payment-service/src/main/resources/application.properties`:
```properties
# 🔑 REPLACE THESE WITH YOUR ACTUAL RAZORPAY KEYS
payment.razorpay.key-id=rzp_test_YOUR_KEY_ID_HERE
payment.razorpay.key-secret=YOUR_SECRET_KEY_HERE
payment.razorpay.webhook-secret=YOUR_WEBHOOK_SECRET_HERE
```

### 3. **Start the Service**
```bash
cd payment-service
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

## 🧪 Comprehensive Testing Guide

### 🔧 **Setup for Testing**

#### 1. **Start All Required Services**
```bash
# Terminal 1: Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 2: API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 3: Task Service
cd task-service && mvn spring-boot:run

# Terminal 4: Profile Service
cd profile-service && mvn spring-boot:run

# Terminal 5: Bidding Service
cd bidding-service && mvn spring-boot:run

# Terminal 6: Payment Service
cd payment-service && mvn spring-boot:run
```

#### 2. **Database Setup**
```sql
-- Run this SQL script
mysql -u root -p < setup-payment-database.sql
```

### 📋 **Postman Testing Examples**

#### **Test 1: Create Payment for Task**
```http
POST http://localhost:8080/api/payments/tasks/1/create
Headers:
  Authorization: Bearer <your-jwt-token>
  Content-Type: application/json

Response:
{
  "success": true,
  "payment": {
    "id": 1,
    "taskId": 1,
    "amount": 100.00,
    "platformFee": 5.00,
    "workerAmount": 95.00,
    "status": "CREATED",
    "razorpayOrderId": "order_ABC123",
    "currency": "INR"
  },
  "escrow": {
    "id": 1,
    "taskId": 1,
    "totalAmount": 100.00,
    "status": "CREATED",
    "expiresAt": "2024-01-15T23:59:59",  // 🔥 TASK DEADLINE
    "canBeReleased": false,
    "canBeRefunded": true
  },
  "razorpayOrder": {
    "id": "order_ABC123",
    "amount": 10000,  // In paise
    "currency": "INR",
    "receipt": "task_1_payment"
  }
}
```

#### **Test 2: Accept Work & Release Payment**
```http
POST http://localhost:8080/api/payments/tasks/1/accept
Headers:
  Authorization: Bearer <client-jwt-token>
  Content-Type: application/json

Body:
{
  "reason": "Work completed successfully, meets all requirements"
}

Response:
{
  "success": true,
  "message": "Work accepted and payment released successfully",
  "payment": {
    "id": 1,
    "status": "COMPLETED",
    "completedAt": "2024-01-10T14:30:00"
  },
  "escrow": {
    "id": 1,
    "status": "RELEASED",
    "releasedAt": "2024-01-10T14:30:00",
    "releaseReason": "Work completed successfully, meets all requirements"
  },
  "transaction": {
    "id": 15,
    "type": "PAYMENT_RELEASED",
    "amount": 95.00,
    "description": "Payment released for completed task"
  }
}
```

#### **Test 3: Reject Work & Refund Payment**
```http
POST http://localhost:8080/api/payments/tasks/1/reject
Headers:
  Authorization: Bearer <client-jwt-token>
  Content-Type: application/json

Body:
{
  "reason": "Work does not meet requirements, needs revision"
}

Response:
{
  "success": true,
  "message": "Work rejected and payment refunded successfully",
  "payment": {
    "id": 1,
    "status": "REFUNDED"
  },
  "escrow": {
    "id": 1,
    "status": "REFUNDED",
    "refundedAt": "2024-01-10T14:30:00",
    "refundReason": "Work does not meet requirements, needs revision"
  },
  "transaction": {
    "id": 16,
    "type": "PAYMENT_REFUNDED",
    "amount": 100.00,
    "description": "Payment refunded for rejected work"
  }
}
```

#### **Test 4: Get User Wallet**
```http
GET http://localhost:8080/api/payments/wallet
Headers:
  Authorization: Bearer <user-jwt-token>

Response:
{
  "success": true,
  "wallet": {
    "id": 5,
    "userId": 123,
    "balance": 1250.00,
    "totalEarned": 2500.00,
    "totalSpent": 1000.00,
    "totalRefunded": 250.00,
    "status": "ACTIVE",
    "lastTransactionAt": "2024-01-10T14:30:00"
  }
}
```

#### **Test 5: Get Transaction History**
```http
GET http://localhost:8080/api/payments/transactions?page=0&size=10
Headers:
  Authorization: Bearer <user-jwt-token>

Response:
{
  "success": true,
  "transactions": [
    {
      "id": 15,
      "type": "PAYMENT_RELEASED",
      "amount": 95.00,
      "status": "COMPLETED",
      "description": "Payment released for completed task",
      "taskId": 1,
      "balanceBefore": 1155.00,
      "balanceAfter": 1250.00,
      "createdAt": "2024-01-10T14:30:00"
    },
    {
      "id": 14,
      "type": "PAYMENT_RECEIVED",
      "amount": 100.00,
      "status": "COMPLETED",
      "description": "Payment for task: Web Development Project",
      "taskId": 1,
      "balanceBefore": 1055.00,
      "balanceAfter": 1155.00,
      "createdAt": "2024-01-10T10:15:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0
}
```

#### **Test 6: Get Escrow Details**
```http
GET http://localhost:8080/api/payments/escrows/task/1
Headers:
  Authorization: Bearer <user-jwt-token>

Response:
{
  "success": true,
  "escrow": {
    "id": 1,
    "taskId": 1,
    "totalAmount": 100.00,
    "platformFee": 5.00,
    "workerAmount": 95.00,
    "status": "FUNDED",
    "expiresAt": "2024-01-15T23:59:59",
    "isExpired": false,
    "canBeReleased": true,
    "canBeRefunded": true,
    "createdAt": "2024-01-10T10:00:00",
    "updatedAt": "2024-01-10T10:15:00"
  }
}
```

### 🎯 **Business Logic Testing Scenarios**

#### **Scenario 1: Normal Payment Flow**
```
1. Client posts task (deadline: Jan 15, 2024)
2. Bidders place bids
3. System selects winning bid automatically
4. Client creates payment → Escrow created (expires: Jan 15, 2024) 🔥
5. Client pays via Razorpay → Escrow funded
6. Worker completes work before deadline
7. Client accepts work → Payment released immediately ✅
```

#### **Scenario 2: Work Rejection Flow**
```
1. Client posts task (deadline: Jan 15, 2024)
2. Payment created and funded
3. Worker submits work before deadline
4. Client rejects work → Refund processed immediately ✅
5. Task reopened for new bids
6. Money returned to client wallet
```

#### **Scenario 3: Deadline Expiration (Automatic)**
```
1. Client posts task (deadline: Jan 15, 2024)
2. Payment created and funded
3. Worker doesn't submit work by deadline
4. Scheduler runs → Detects expired deadline
5. Auto-refund processed → Money returned to client ✅
6. Task marked as failed and reopened
```

#### **Scenario 4: Platform Fee Calculation**
```
Task Amount: ₹100
Platform Fee (5%): ₹5
Worker Receives: ₹95
Client Pays: ₹100

Escrow Breakdown:
- Total Amount: ₹100 (held in escrow)
- Platform Fee: ₹5 (deducted on release)
- Worker Amount: ₹95 (released to worker)
```

### 🔧 **Admin Testing**

#### **Manual Deadline Check**
```http
POST http://localhost:8080/api/payments/admin/trigger-deadline-check
Headers:
  Authorization: Bearer <admin-jwt-token>

Response:
{
  "success": true,
  "message": "Deadline check completed",
  "processed": 3,
  "refunded": 1,
  "details": [
    {
      "taskId": 5,
      "escrowId": 8,
      "action": "REFUNDED",
      "amount": 150.00,
      "reason": "Task deadline expired"
    }
  ]
}
```

#### **Get Scheduler Configuration**
```http
GET http://localhost:8080/api/payments/admin/config
Headers:
  Authorization: Bearer <admin-jwt-token>

Response:
{
  "success": true,
  "configuration": "Auto-check interval: 3600000 ms (1 hours), Notifications: true",
  "nextRun": "2024-01-10T15:00:00",
  "lastRun": "2024-01-10T14:00:00",
  "totalChecks": 156,
  "totalRefunds": 23
}
```

### 🚨 **Error Handling Examples**

#### **Insufficient Balance**
```http
POST http://localhost:8080/api/payments/tasks/1/create

Response (400):
{
  "success": false,
  "error": "Insufficient wallet balance",
  "message": "Required: ₹100.00, Available: ₹50.00",
  "code": "INSUFFICIENT_BALANCE"
}
```

#### **Escrow Already Released**
```http
POST http://localhost:8080/api/payments/tasks/1/accept

Response (400):
{
  "success": false,
  "error": "Cannot accept work",
  "message": "Escrow has already been released",
  "code": "ESCROW_ALREADY_RELEASED"
}
```

#### **Task Deadline Expired**
```http
POST http://localhost:8080/api/payments/tasks/1/accept

Response (400):
{
  "success": false,
  "error": "Cannot accept work",
  "message": "Task deadline has expired. Payment has been automatically refunded.",
  "code": "TASK_DEADLINE_EXPIRED"
}
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
spring.datasource.url=jdbc:mysql://localhost:3306/campusworks_payment_db
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

### 🏦 **Financial Integrity**
- **Double-entry accounting**: Every transaction creates corresponding entries
- **Balance validation**: All operations validate sufficient balances
- **Atomic transactions**: Database operations are wrapped in transactions
- **Audit trail**: Complete transaction history for all users

### 🔒 **Security Features**
- **JWT Authentication**: All endpoints require valid JWT tokens
- **User Authorization**: Users can only access their own financial data
- **Webhook Verification**: Razorpay webhooks are cryptographically verified
- **Circuit Breakers**: Graceful handling of service failures

## 📈 Next Steps

1. **Testing**: Complete testing with Razorpay test environment ✅
2. **Notification Service**: Integrate for payment notifications
3. **Frontend Integration**: Connect with React frontend
4. **Production**: Switch to live Razorpay keys for production
5. **Analytics**: Add payment analytics and reporting features

---

**🎉 Payment Gateway Service is fully implemented with your custom escrow logic! The system handles payments exactly as specified - tied to task deadlines with immediate actions on acceptance/rejection. All business logic, database entities, API endpoints, and comprehensive testing examples are documented above.**