# CampusWorks - Phase 1 Implementation Guide

## 🎯 **Phase 1 Overview**

Phase 1 implements the core infrastructure for CampusWorks:
- **Eureka Server** - Service discovery and registration
- **API Gateway** - Centralized routing and JWT authentication
- **Auth Service** - User management and JWT generation

## 🏗️ **Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Eureka Server │    │  API Gateway    │    │  Auth Service   │
│   Port: 8761    │◄──►│   Port: 8080    │◄──►│   Port: 9001    │
│                 │    │                 │    │                 │
│ • Service Reg   │    │ • JWT Auth      │    │ • User Reg      │
│ • Discovery     │    │ • Routing       │    │ • Login         │
│ • Health Check  │    │ • CORS          │    │ • JWT Gen       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 **Quick Start**

### **Prerequisites**
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Windows (for .bat scripts)

### **1. Database Setup**
```sql
-- Run the setup-database.sql script in MySQL
-- Or manually create the database:
CREATE DATABASE campusworks_auth;
```

### **2. Start Services**
```bash
# Run the startup script
start-phase1.bat

# Or start manually:
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run  
cd auth-service && mvn spring-boot:run
```

### **3. Test Services**
```bash
# Run the testing script
test-phase1.bat
```

## 📍 **Service URLs**

| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| **Eureka Server** | 8761 | http://localhost:8761 | Service discovery dashboard |
| **API Gateway** | 8080 | http://localhost:8080 | Central entry point |
| **Auth Service** | 9001 | http://localhost:9001 | Direct access to auth |
| **Gateway Auth** | 8080 | http://localhost:8080/api/auth | Auth through gateway |

## 🔐 **Authentication Flow**

### **1. User Registration**
```bash
# Both paths work now:
POST http://localhost:8080/auth/register
# OR
POST http://localhost:8080/api/auth/register

Content-Type: application/json

{
  "email": "student@campus.edu",
  "password": "student123"
}
```

**Response:**
```json
{
  "message": "User registered successfully as STUDENT",
  "userId": 1,
  "email": "student@campus.edu",
  "role": "STUDENT"
}
```

### **2. User Login**
```bash
# Both paths work now:
POST http://localhost:8080/auth/login
# OR
POST http://localhost:8080/api/auth/login

Content-Type: application/json

{
  "email": "student@campus.edu",
  "password": "student123"
}
```

**Response:**
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "student@campus.edu"
}
```

### **3. Admin Login**
```bash
# Both paths work now:
POST http://localhost:8080/auth/login
# OR
POST http://localhost:8080/api/auth/login

Content-Type: application/json

{
  "email": "admin@campusworks.com",
  "password": "admin123"
}
```

## 🎭 **Role System**

### **Simplified Approach (As per Blueprint)**
- **STUDENT**: Default role for all new registrations
  - Can post tasks (as someone needing help)
  - Can bid on tasks (as someone helping others)
  - Flexible role that covers all user activities
- **ADMIN**: System administrator
  - Created automatically on startup
  - Full system access
  - Only one admin exists

### **No WORKER Role Needed**
- Students naturally act as workers when bidding
- Business logic determines context
- Simpler and more realistic approach

## 🔒 **Security Features**

### **JWT Implementation**
- **Secret Key**: `mysupersecuresecretkeythatismorethan32chars`
- **Expiration**: 24 hours (86400000 ms)
- **Algorithm**: HS256 (HMAC with SHA-256)

### **API Gateway Security**
- JWT validation for all protected endpoints
- Public endpoints: `/auth/**`, `/actuator/**`
- User context propagation via headers:
  - `X-User-Id`: User's unique identifier
  - `X-User-Email`: User's email address
  - `X-User-Roles`: User's role

### **CORS Configuration**
- Global CORS at API Gateway level
- All origins allowed for development
- All HTTP methods supported
- Credentials enabled

## 🗄️ **Database Schema**

### **Users Table**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 🧪 **Testing**

### **Health Checks**
```bash
# Eureka Server
curl http://localhost:8761/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# Auth Service (direct)
curl http://localhost:9001/auth/health

# Auth Service (through gateway - both paths work)
curl http://localhost:8080/auth/health
curl http://localhost:8080/api/auth/health
```

### **Expected Responses**
- **Eureka Server**: `{"status":"UP"}`
- **API Gateway**: `{"status":"UP"}`
- **Auth Service**: `"Auth Service is running - Phase 1 ✅"`

## 📋 **API Endpoints**

### **Public Endpoints (No JWT Required)**
- `GET /auth/health` - Service health check (both `/auth/health` and `/api/auth/health`)
- `POST /auth/register` - User registration (both `/auth/register` and `/api/auth/register`)
- `POST /auth/login` - User authentication (both `/auth/login` and `/api/auth/login`)
- `GET /auth/user/{email}` - Get user by email (both `/auth/user/{email}` and `/api/auth/user/{email}`)

### **Protected Endpoints (JWT Required)**
- All other endpoints require valid JWT token
- Token format: `Authorization: Bearer <jwt_token>`

## 🔧 **Recent Fixes Applied**

### **1. CORS Configuration Fix**
- **Issue**: CORS error when `allowCredentials=true` with `allowed-origins=*`
- **Solution**: Changed to `allowed-origin-patterns=*` for compatibility
- **Result**: Both CORS and credentials now work properly

### **2. Gateway Route Enhancement**
- **Issue**: Only `/api/auth/**` routes were working
- **Solution**: Added direct `/auth/**` route for flexibility
- **Result**: Both `/auth/**` and `/api/auth/**` paths now work

### **3. Updated Configuration**
```properties
# Both routes now available:
spring.cloud.gateway.routes[0].id=auth-service-api
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**

spring.cloud.gateway.routes[1].id=auth-service-direct  
spring.cloud.gateway.routes[1].predicates[0]=Path=/auth/**

# Fixed CORS:
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origin-patterns=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials=true
```

---

## 🐛 **Troubleshooting**

### **Common Issues**

#### **1. Service Not Starting**
- Check Java version: `java -version` (should be 17+)
- Check Maven: `mvn -version`
- Check MySQL connection
- Check port availability

#### **2. Database Connection Issues**
- Ensure MySQL is running
- Check credentials in `application.properties`
- Verify database exists: `SHOW DATABASES;`

#### **3. JWT Authentication Failures**
- Verify JWT secret matches across services
- Check token expiration
- Ensure Authorization header format: `Bearer <token>`

#### **4. Service Discovery Issues**
- Check Eureka server is running
- Verify service names in `application.properties`
- Check network connectivity

### **Debug Commands**
```bash
# Check service registration
curl http://localhost:8761/eureka/apps

# Test JWT token (both paths work)
curl -H "Authorization: Bearer <your_token>" http://localhost:8080/auth/user/student@campus.edu
curl -H "Authorization: Bearer <your_token>" http://localhost:8080/api/auth/user/student@campus.edu

# Check service logs
# Look for emojis and clear log messages in console output
```

## 🚀 **Next Steps**

After Phase 1 is working correctly:

1. **Test all endpoints** using the test script
2. **Verify JWT authentication** works through API Gateway
3. **Confirm service discovery** is working
4. **Move to Phase 2**: Task Service, Bidding Service, Profile Service

## 📚 **References**

- [CampusWorks Project Blueprint](./CampusWorks_Project_Blueprint.md)
- [Spring Boot 3.4.0 Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud 2024.0.1 Documentation](https://spring.io/projects/spring-cloud)
- [JWT.io](https://jwt.io/) - JWT token debugging

---

## ✅ **Phase 1 Success Criteria**

- [ ] Eureka Server starts and shows dashboard
- [ ] API Gateway starts and routes to Auth Service
- [ ] Auth Service starts and connects to database
- [ ] User registration works (creates STUDENT role)
- [ ] User login works (returns JWT token)
- [ ] Admin login works (default credentials)
- [ ] JWT authentication works through gateway
- [ ] Service discovery is working
- [ ] Health checks return UP status
- [ ] CORS is properly configured

**Phase 1 is complete when all services start successfully and basic authentication works! 🎉**
