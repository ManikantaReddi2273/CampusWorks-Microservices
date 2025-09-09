# 🚂 Railway Deployment Guide for CampusWorks

This guide shows how to deploy your CampusWorks project on Railway platform using the provided configuration files.

## 📋 **Railway Configuration Files Created**

### ✅ **Backend Services (6 files)**
- `campus-works-backend/railway.json` - Main backend configuration
- `campus-works-backend/eureka-server/railway.json` - Eureka Server
- `campus-works-backend/api-gateway/railway.json` - API Gateway
- `campus-works-backend/auth-service/railway.json` - Auth Service
- `campus-works-backend/task-service/railway.json` - Task Service
- `campus-works-backend/bidding-service/railway.json` - Bidding Service
- `campus-works-backend/profile-service/railway.json` - Profile Service

### ✅ **Chat Service (1 file)**
- `campusworks-chat-service/railway.json` - Chat Service

---

## 🔧 **Railway Configuration Details**

### **Backend Services Configuration**
```json
{
  "build": {
    "builder": "NIXPACKS"
  },
  "deploy": {
    "startCommand": "java -jar target/*.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

### **Chat Service Configuration**
```json
{
  "build": {
    "builder": "NIXPACKS"
  },
  "deploy": {
    "startCommand": "npm start",
    "healthcheckPath": "/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

---

## 🚀 **Step-by-Step Railway Deployment**

### **Step 1: Prepare Your Repository**

1. **Commit all changes:**
   ```bash
   git add .
   git commit -m "Add Railway configuration files"
   git push origin main
   ```

2. **Ensure all services have:**
   - ✅ `railway.json` configuration file
   - ✅ `Dockerfile` for containerization
   - ✅ Environment variables configured

### **Step 2: Create Railway Account & Project**

1. **Go to [Railway](https://railway.app)**
2. **Sign up/Login** with GitHub
3. **Create new project** from GitHub repository
4. **Select your CampusWorks repository**

### **Step 3: Deploy Eureka Server First**

1. **Create new service** in Railway
2. **Select `campus-works-backend/eureka-server`** directory
3. **Add environment variables:**
   ```bash
   SERVICE_PORT=8761
   SERVICE_NAME=eureka-server
   EUREKA_HOSTNAME=0.0.0.0
   SPRING_PROFILES_ACTIVE=production
   ```
4. **Deploy** - Get URL: `https://eureka-server-production.railway.app`

### **Step 4: Deploy API Gateway**

1. **Create new service** in Railway
2. **Select `campus-works-backend/api-gateway`** directory
3. **Add environment variables:**
   ```bash
   SERVICE_PORT=8080
   SERVICE_NAME=api-gateway
   EUREKA_URL=https://eureka-server-production.railway.app/eureka/
   EUREKA_HOSTNAME=api-gateway
   JWT_SECRET=your_production_jwt_secret
   JWT_EXPIRATION=86400000
   SPRING_PROFILES_ACTIVE=production
   ```
4. **Deploy** - Get URL: `https://api-gateway-production.railway.app`

### **Step 5: Deploy Backend Services**

#### **Auth Service**
1. **Create new service** in Railway
2. **Select `campus-works-backend/auth-service`** directory
3. **Add environment variables:**
   ```bash
   SERVICE_PORT=9000
   SERVICE_NAME=auth-service
   DB_HOST=your_production_db_host
   DB_PORT=3306
   DB_NAME=campusworks_auth
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   DB_SSL=true
   EUREKA_URL=https://eureka-server-production.railway.app/eureka/
   EUREKA_HOSTNAME=auth-service
   JWT_SECRET=your_production_jwt_secret
   JWT_EXPIRATION=86400000
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your_email@gmail.com
   SMTP_PASSWORD=your_app_password
   FRONTEND_URL=https://your-frontend-url.vercel.app
   SPRING_PROFILES_ACTIVE=production
   ```

#### **Task Service**
1. **Create new service** in Railway
2. **Select `campus-works-backend/task-service`** directory
3. **Add environment variables:**
   ```bash
   SERVICE_PORT=9001
   SERVICE_NAME=task-service
   DB_HOST=your_production_db_host
   DB_PORT=3306
   DB_NAME=campusworks_tasks
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   DB_SSL=true
   EUREKA_URL=https://eureka-server-production.railway.app/eureka/
   EUREKA_HOSTNAME=task-service
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your_email@gmail.com
   SMTP_PASSWORD=your_app_password
   SPRING_PROFILES_ACTIVE=production
   ```

#### **Bidding Service**
1. **Create new service** in Railway
2. **Select `campus-works-backend/bidding-service`** directory
3. **Add environment variables:**
   ```bash
   SERVICE_PORT=9002
   SERVICE_NAME=bidding-service
   DB_HOST=your_production_db_host
   DB_PORT=3306
   DB_NAME=campusworks_bids
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   DB_SSL=true
   EUREKA_URL=https://eureka-server-production.railway.app/eureka/
   EUREKA_HOSTNAME=bidding-service
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your_email@gmail.com
   SMTP_PASSWORD=your_app_password
   SPRING_PROFILES_ACTIVE=production
   ```

#### **Profile Service**
1. **Create new service** in Railway
2. **Select `campus-works-backend/profile-service`** directory
3. **Add environment variables:**
   ```bash
   SERVICE_PORT=9003
   SERVICE_NAME=profile-service
   DB_HOST=your_production_db_host
   DB_PORT=3306
   DB_NAME=campusworks_profile
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   DB_SSL=true
   EUREKA_URL=https://eureka-server-production.railway.app/eureka/
   EUREKA_HOSTNAME=profile-service
   SPRING_PROFILES_ACTIVE=production
   ```

### **Step 6: Deploy Chat Service**

1. **Create new service** in Railway
2. **Select `campusworks-chat-service`** directory
3. **Add environment variables:**
   ```bash
   PORT=3001
   HOST=0.0.0.0
   SPRING_BOOT_API_URL=https://api-gateway-production.railway.app
   CORS_ORIGIN=https://your-frontend-url.vercel.app
   MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/campusworks_chat
   JWT_SECRET=your_production_jwt_secret
   JWT_EXPIRES_IN=24h
   NODE_ENV=production
   ```

---

## 🔧 **Railway Configuration Features**

### **✅ NIXPACKS Builder**
- **Automatic detection** of Java/Node.js projects
- **Smart dependency resolution**
- **Optimized builds** for production

### **✅ Health Checks**
- **Spring Boot:** `/actuator/health` endpoint
- **Node.js:** `/health` endpoint
- **300-second timeout** for startup
- **Automatic restart** on failure

### **✅ Restart Policies**
- **ON_FAILURE** restart policy
- **Maximum 10 retries**
- **Automatic recovery** from crashes

### **✅ Port Configuration**
- **Dynamic port assignment** by Railway
- **Environment variable** `PORT` for services
- **Automatic HTTPS** termination

---

## 🌐 **Service URLs After Deployment**

After successful deployment, you'll get URLs like:
- **Eureka Server:** `https://eureka-server-production.railway.app`
- **API Gateway:** `https://api-gateway-production.railway.app`
- **Auth Service:** `https://auth-service-production.railway.app`
- **Task Service:** `https://task-service-production.railway.app`
- **Bidding Service:** `https://bidding-service-production.railway.app`
- **Profile Service:** `https://profile-service-production.railway.app`
- **Chat Service:** `https://chat-service-production.railway.app`

---

## 🔍 **Monitoring & Debugging**

### **Railway Dashboard**
- **Service logs** in real-time
- **Resource usage** monitoring
- **Deployment history**
- **Environment variables** management

### **Health Check Endpoints**
- **Eureka:** `https://eureka-server-production.railway.app/actuator/health`
- **API Gateway:** `https://api-gateway-production.railway.app/actuator/health`
- **Services:** `https://service-name-production.railway.app/actuator/health`
- **Chat Service:** `https://chat-service-production.railway.app/health`

---

## 💰 **Railway Pricing**

### **Free Tier (Perfect for CampusWorks)**
- **$5 credit** per month
- **512MB RAM** per service
- **1GB storage**
- **Unlimited deployments**
- **Custom domains**

### **Usage Estimation**
- **7 services** × **512MB** = **3.5GB RAM**
- **Well within free tier** limits
- **No additional costs** for basic usage

---

## 🚀 **Deployment Order**

1. **Eureka Server** (must be first)
2. **API Gateway** (depends on Eureka)
3. **Backend Services** (can be deployed in parallel)
4. **Chat Service** (depends on API Gateway)

---

## ✅ **Benefits of Railway Deployment**

1. **Zero Configuration** - Just push code
2. **Automatic Scaling** - Handles traffic spikes
3. **Built-in Monitoring** - Real-time logs and metrics
4. **Easy Environment Management** - Simple variable updates
5. **GitHub Integration** - Automatic deployments
6. **Free Tier** - Perfect for student projects
7. **HTTPS by Default** - Secure out of the box

---

## 🎯 **Next Steps After Deployment**

1. **Test all services** using the provided URLs
2. **Update frontend** environment variables
3. **Configure custom domains** (optional)
4. **Set up monitoring** alerts
5. **Configure CI/CD** for automatic deployments

Your CampusWorks project is now **Railway-ready** for production deployment! 🚂🎉
