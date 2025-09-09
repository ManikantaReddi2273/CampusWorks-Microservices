# 🌍 CampusWorks Environment Configuration Guide

This guide shows how to configure your CampusWorks project using environment variables instead of hardcoded localhost references.

## 📋 **What Was Changed**

### ✅ **Backend Services (Spring Boot)**
- All `application.properties` files now use environment variables
- Database URLs, Eureka URLs, SMTP settings, and JWT secrets are configurable
- Default values provided for local development

### ✅ **Frontend (React + Vite)**
- API base URL and chat service URL use environment variables
- Vite proxy configuration uses environment variables
- All constants updated to use `import.meta.env`

### ✅ **Chat Service (Node.js)**
- Already using environment variables (no changes needed)

## 🔧 **Environment Variables Setup**

### **1. Backend Services (.env files)**

Create `.env` files in each service directory:

#### **Auth Service** (`campus-works-backend/auth-service/.env`)
```bash
# Service Configuration
SERVICE_PORT=9000
SERVICE_NAME=auth-service

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=campusworks_auth
DB_USERNAME=root
DB_PASSWORD=reddi2273
DB_SSL=false

# Eureka Configuration
EUREKA_URL=http://localhost:8761/eureka/
EUREKA_HOSTNAME=localhost
EUREKA_PREFER_IP=false
EUREKA_IP=127.0.0.1

# JWT Configuration
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
JWT_EXPIRATION=86400000

# SMTP Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=campusworks2273@gmail.com
SMTP_PASSWORD=nlbdkysxhffrjwnt

# Application Configuration
APP_NAME=CampusWorks
FRONTEND_URL=http://localhost:3000
VERIFICATION_TOKEN_EXPIRY=24

# JPA Configuration
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true
```

#### **Task Service** (`campus-works-backend/task-service/.env`)
```bash
# Service Configuration
SERVICE_PORT=9001
SERVICE_NAME=task-service

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=campusworks_tasks
DB_USERNAME=root
DB_PASSWORD=reddi2273
DB_SSL=false

# Eureka Configuration
EUREKA_URL=http://localhost:8761/eureka/
EUREKA_HOSTNAME=localhost
EUREKA_PREFER_IP=false
EUREKA_IP=127.0.0.1

# SMTP Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=campusworks2273@gmail.com
SMTP_PASSWORD=nlbdkysxhffrjwnt

# JPA Configuration
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true
```

#### **Bidding Service** (`campus-works-backend/bidding-service/.env`)
```bash
# Service Configuration
SERVICE_PORT=9002
SERVICE_NAME=bidding-service

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=campusworks_bids
DB_USERNAME=root
DB_PASSWORD=reddi2273
DB_SSL=false

# Eureka Configuration
EUREKA_URL=http://localhost:8761/eureka/
EUREKA_HOSTNAME=localhost
EUREKA_PREFER_IP=false
EUREKA_IP=127.0.0.1

# SMTP Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=campusworks2273@gmail.com
SMTP_PASSWORD=nlbdkysxhffrjwnt

# JPA Configuration
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true
```

#### **Profile Service** (`campus-works-backend/profile-service/.env`)
```bash
# Service Configuration
SERVICE_PORT=9003
SERVICE_NAME=profile-service

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=campusworks_profile
DB_USERNAME=root
DB_PASSWORD=reddi2273
DB_SSL=false

# Eureka Configuration
EUREKA_URL=http://localhost:8761/eureka/
EUREKA_HOSTNAME=localhost
EUREKA_PREFER_IP=false
EUREKA_IP=127.0.0.1

# JPA Configuration
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true
```

#### **API Gateway** (`campus-works-backend/api-gateway/.env`)
```bash
# Service Configuration
SERVICE_PORT=8080
SERVICE_NAME=api-gateway

# Eureka Configuration
EUREKA_URL=http://localhost:8761/eureka/
EUREKA_HOSTNAME=localhost
EUREKA_PREFER_IP=true

# JWT Configuration
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
JWT_EXPIRATION=86400000
```

#### **Eureka Server** (`campus-works-backend/eureka-server/.env`)
```bash
# Service Configuration
SERVICE_PORT=8761
SERVICE_NAME=eureka-server

# Eureka Configuration
EUREKA_HOSTNAME=localhost
```

### **2. Frontend** (`campus-works-frontend/.env`)
```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080
VITE_CHAT_SERVICE_URL=http://localhost:3001

# Development Configuration
VITE_DEV_PORT=3000

# App Configuration
VITE_APP_NAME=CampusWorks
VITE_APP_VERSION=1.0.0

# Payment Configuration
VITE_RAZORPAY_KEY_ID=your_razorpay_key_id_here

# Development Tools
VITE_ENABLE_REDUX_DEVTOOLS=true
```

### **3. Chat Service** (`campusworks-chat-service/.env`)
```bash
# Server Configuration
PORT=3001
HOST=localhost

# CORS Configuration
CORS_ORIGIN=http://localhost:3000

# Spring Boot API Configuration
SPRING_BOOT_API_URL=http://localhost:8080

# JWT Configuration
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
JWT_EXPIRES_IN=24h

# Logging Configuration
LOG_LEVEL=info
```

## 🚀 **Production Environment Variables**

For production deployment, update these variables:

### **Database (Production)**
```bash
DB_HOST=your_production_db_host
DB_PORT=3306
DB_USERNAME=your_production_db_user
DB_PASSWORD=your_production_db_password
DB_SSL=true
```

### **Eureka (Production)**
```bash
EUREKA_URL=http://your_eureka_host:8761/eureka/
EUREKA_HOSTNAME=your_eureka_host
EUREKA_PREFER_IP=true
```

### **Frontend (Production)**
```bash
VITE_API_BASE_URL=https://your_api_domain.com
VITE_CHAT_SERVICE_URL=https://your_chat_domain.com
```

### **SMTP (Production)**
```bash
SMTP_HOST=your_smtp_host
SMTP_PORT=587
SMTP_USERNAME=your_production_email
SMTP_PASSWORD=your_production_email_password
```

## 🔒 **Security Notes**

1. **Never commit .env files to version control**
2. **Use strong, unique JWT secrets in production**
3. **Use environment-specific database credentials**
4. **Enable SSL for production databases**
5. **Use HTTPS URLs in production**

## 📝 **How to Use**

1. **Create .env files** in each service directory with the configurations above
2. **Update values** according to your environment
3. **Start services** - they will automatically use the environment variables
4. **For production**, update all URLs and credentials to production values

## ✅ **Benefits**

- ✅ **No more hardcoded localhost references**
- ✅ **Easy environment switching** (dev/staging/prod)
- ✅ **Secure credential management**
- ✅ **Deployment-ready configuration**
- ✅ **Consistent across all services**

## 🧪 **Testing**

After setting up environment variables:

1. **Start Eureka Server** - should register on configured port
2. **Start API Gateway** - should connect to Eureka
3. **Start other services** - should register with Eureka
4. **Start Frontend** - should connect to API Gateway
5. **Start Chat Service** - should connect to API Gateway

All services will now use environment variables instead of hardcoded values!
