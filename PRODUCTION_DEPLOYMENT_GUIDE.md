# 🚀 CampusWorks Production Deployment Guide

This guide shows how to deploy your CampusWorks project with your actual database configurations.

## 📋 **Your Database Configurations**

### **MySQL Database (Railway)**
```
Host: tramway.proxy.rlwy.net
Port: 33729
Username: root
Password: vLlLZQMmEqPUkNuEoRAmMQNydvmADeFl
Database: railway
```

### **MongoDB Database (Atlas)**
```
Connection String: mongodb+srv://n210419_db_user:MekxRNW6Pg0JLb3b@cluster0.xrjmxy9.mongodb.net/campusworks_chat?retryWrites=true&w=majority&appName=Cluster0
```

---

## 🗄️ **Step 1: Create Database Schemas**

### **1.1 Connect to MySQL Database**
Use MySQL Workbench with these details:
- **Host:** `tramway.proxy.rlwy.net`
- **Port:** `33729`
- **Username:** `root`
- **Password:** `vLlLZQMmEqPUkNuEoRAmMQNydvmADeFl`
- **Database:** `railway`

### **1.2 Create Database Schemas**
Run these SQL commands:
```sql
-- Create database schemas for each service
CREATE DATABASE IF NOT EXISTS campusworks_auth;
CREATE DATABASE IF NOT EXISTS campusworks_tasks;
CREATE DATABASE IF NOT EXISTS campusworks_bids;
CREATE DATABASE IF NOT EXISTS campusworks_profile;
CREATE DATABASE IF NOT EXISTS campusworks_payments;

-- Verify databases were created
SHOW DATABASES;
```

---

## 🚂 **Step 2: Deploy Backend Services to Railway**

### **2.1 Deploy Eureka Server**
1. **Go to [Railway](https://railway.app)**
2. **Create new project**
3. **Deploy from GitHub repo**
4. **Select `campus-works-backend/eureka-server`**
5. **Environment variables are already configured in `railway.json`**

### **2.2 Deploy API Gateway**
1. **Create new service in Railway**
2. **Select `campus-works-backend/api-gateway`**
3. **Environment variables are already configured**

### **2.3 Deploy Auth Service**
1. **Create new service in Railway**
2. **Select `campus-works-backend/auth-service`**
3. **Environment variables are already configured**

### **2.4 Deploy Task Service**
1. **Create new service in Railway**
2. **Select `campus-works-backend/task-service`**
3. **Environment variables are already configured**

### **2.5 Deploy Bidding Service**
1. **Create new service in Railway**
2. **Select `campus-works-backend/bidding-service`**
3. **Environment variables are already configured**

### **2.6 Deploy Profile Service**
1. **Create new service in Railway**
2. **Select `campus-works-backend/profile-service`**
3. **Environment variables are already configured**

---

## 💬 **Step 3: Deploy Chat Service to Railway**

### **3.1 Deploy Chat Service**
1. **Create new service in Railway**
2. **Select `campusworks-chat-service`**
3. **Environment variables are already configured with MongoDB**

---

## 🎨 **Step 4: Deploy Frontend to Vercel**

### **4.1 Deploy to Vercel**
1. **Go to [Vercel](https://vercel.com)**
2. **Connect your GitHub repository**
3. **Select `campus-works-frontend`**
4. **Add environment variables:**
   ```bash
   VITE_API_BASE_URL=https://api-gateway-production.railway.app
   VITE_CHAT_SERVICE_URL=https://chat-service-production.railway.app
   VITE_APP_NAME=CampusWorks
   VITE_APP_VERSION=1.0.0
   VITE_RAZORPAY_KEY_ID=your_razorpay_key_id
   VITE_ENABLE_REDUX_DEVTOOLS=false
   ```

---

## 🔧 **Step 5: Update Service URLs**

After deployment, you'll get URLs like:
- **Eureka Server:** `https://eureka-server-production.railway.app`
- **API Gateway:** `https://api-gateway-production.railway.app`
- **Auth Service:** `https://auth-service-production.railway.app`
- **Task Service:** `https://task-service-production.railway.app`
- **Bidding Service:** `https://bidding-service-production.railway.app`
- **Profile Service:** `https://profile-service-production.railway.app`
- **Chat Service:** `https://chat-service-production.railway.app`
- **Frontend:** `https://campusworks-frontend.vercel.app`

### **5.1 Update Eureka URLs**
Update the `EUREKA_URL` in all services to use your actual Eureka server URL.

### **5.2 Update Frontend URLs**
Update the `FRONTEND_URL` in all backend services to use your actual frontend URL.

---

## 🧪 **Step 6: Test Your Deployment**

### **6.1 Test Service Health**
1. **Eureka Server:** `https://eureka-server-production.railway.app`
2. **API Gateway:** `https://api-gateway-production.railway.app/actuator/health`
3. **Auth Service:** `https://auth-service-production.railway.app/actuator/health`
4. **Chat Service:** `https://chat-service-production.railway.app/health`

### **6.2 Test Frontend**
1. **Visit your Vercel URL**
2. **Test registration/login**
3. **Test task creation**
4. **Test bidding functionality**
5. **Test chat functionality**

---

## 🔒 **Step 7: Security Configuration**

### **7.1 Update JWT Secret**
Replace `your_production_jwt_secret_here_must_be_at_least_32_characters` with a strong, unique JWT secret in all services.

### **7.2 Update SMTP Credentials**
Update the SMTP credentials in your services if needed.

---

## 📊 **Environment Variables Summary**

### **Backend Services (MySQL)**
```bash
# Database Configuration
DB_HOST=tramway.proxy.rlwy.net
DB_PORT=33729
DB_USERNAME=root
DB_PASSWORD=vLlLZQMmEqPUkNuEoRAmMQNydvmADeFl
DB_SSL=false

# Individual Database Names
AUTH_DB_NAME=campusworks_auth
TASK_DB_NAME=campusworks_tasks
BIDDING_DB_NAME=campusworks_bids
PROFILE_DB_NAME=campusworks_profile
PAYMENT_DB_NAME=campusworks_payments
```

### **Chat Service (MongoDB)**
```bash
# MongoDB Configuration
MONGODB_URI=mongodb+srv://n210419_db_user:MekxRNW6Pg0JLb3b@cluster0.xrjmxy9.mongodb.net/campusworks_chat?retryWrites=true&w=majority&appName=Cluster0
```

### **Frontend (Vercel)**
```bash
# API Configuration
VITE_API_BASE_URL=https://api-gateway-production.railway.app
VITE_CHAT_SERVICE_URL=https://chat-service-production.railway.app
```

---

## ✅ **Deployment Checklist**

- [ ] MySQL database schemas created
- [ ] Eureka server deployed
- [ ] API Gateway deployed
- [ ] All backend services deployed
- [ ] Chat service deployed
- [ ] Frontend deployed
- [ ] All services are healthy
- [ ] Frontend can connect to backend
- [ ] Database connections working
- [ ] JWT secrets updated
- [ ] SMTP configuration updated

---

## 🆘 **Troubleshooting**

### **Common Issues**

1. **Database Connection Failed:**
   - Check if database schemas are created
   - Verify connection details are correct
   - Check if MySQL service is running

2. **Service Won't Start:**
   - Check service logs in Railway
   - Verify all environment variables are set
   - Check if Eureka server is running first

3. **CORS Errors:**
   - Update CORS_ORIGIN in chat service
   - Update FRONTEND_URL in backend services
   - Check if frontend URL is correct

4. **MongoDB Connection Failed:**
   - Verify MongoDB Atlas cluster is running
   - Check connection string format
   - Ensure IP whitelist includes Railway IPs

---

## 🎯 **Next Steps**

1. **Deploy all services** using the Railway configurations
2. **Test all functionality** end-to-end
3. **Monitor service health** and logs
4. **Set up monitoring** and alerts
5. **Configure custom domains** (optional)

Your CampusWorks project is now ready for production deployment! 🚀🎉
