# 🐳 CampusWorks Docker Deployment Guide

This guide shows how to deploy your CampusWorks project using Docker containers.

## 📋 **What Was Created**

### ✅ **Dockerfiles Created**
- **Backend Services** (6 services) - Spring Boot with Java 17
- **Frontend** - React + Vite with Nginx
- **Chat Service** - Node.js with Express

### ✅ **Docker Compose Files**
- **`docker-compose.yml`** - Local development
- **`docker-compose.prod.yml`** - Production deployment

### ✅ **Configuration Files**
- **`nginx.conf`** - Frontend web server configuration
- **`.gitignore`** - Updated with Docker exclusions

---

## 🚀 **Quick Start**

### **1. Local Development**

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### **2. Production Deployment**

```bash
# Create production environment file
cp .env.example .env.prod

# Update production values in .env.prod
# Then start production services
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📁 **File Structure**

```
CampusWorks-Microservices/
├── docker-compose.yml              # Local development
├── docker-compose.prod.yml         # Production deployment
├── .gitignore                      # Updated with Docker exclusions
├── campus-works-backend/
│   ├── auth-service/
│   │   └── Dockerfile
│   ├── task-service/
│   │   └── Dockerfile
│   ├── bidding-service/
│   │   └── Dockerfile
│   ├── profile-service/
│   │   └── Dockerfile
│   ├── api-gateway/
│   │   └── Dockerfile
│   └── eureka-server/
│       └── Dockerfile
├── campus-works-frontend/
│   ├── Dockerfile
│   └── nginx.conf
└── campusworks-chat-service/
    └── Dockerfile
```

---

## 🔧 **Service Configuration**

### **Backend Services (Spring Boot)**
- **Base Image:** `openjdk:17-jdk-slim` (build) + `openjdk:17-jre-slim` (runtime)
- **Build Tool:** Maven
- **Ports:** 8761 (Eureka), 8080 (Gateway), 9000-9003 (Services)
- **Health Checks:** Built-in actuator endpoints

### **Frontend (React + Vite)**
- **Base Image:** `node:18-alpine` (build) + `nginx:alpine` (runtime)
- **Build Tool:** Vite
- **Port:** 80 (Nginx)
- **Features:** Gzip compression, security headers, SPA routing

### **Chat Service (Node.js)**
- **Base Image:** `node:18-alpine`
- **Port:** 3001
- **Features:** Non-root user, health checks

---

## 🌐 **Network Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Eureka Server │
│   (Nginx:80)    │◄──►│   (Spring:8080) │◄──►│   (Spring:8761) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   Backend       │
                       │   Services      │
                       │   (Spring:9000+)│
                       └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   Databases     │
                       │   MySQL +       │
                       │   MongoDB       │
                       └─────────────────┘
```

---

## 🗄️ **Database Configuration**

### **MySQL (Backend Services)**
- **Image:** `mysql:8.0`
- **Port:** 3306
- **Databases:** 
  - `campusworks_auth`
  - `campusworks_tasks`
  - `campusworks_bids`
  - `campusworks_profile`

### **MongoDB (Chat Service)**
- **Image:** `mongo:6.0`
- **Port:** 27017
- **Database:** `campusworks_chat`

---

## 🔧 **Environment Variables**

### **Local Development (.env)**
```bash
# Database
DB_HOST=mysql
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=reddi2273

# Eureka
EUREKA_URL=http://eureka-server:8761/eureka/

# Frontend
VITE_API_BASE_URL=http://localhost:8080
VITE_CHAT_SERVICE_URL=http://localhost:3001

# MongoDB
MONGODB_URI=mongodb://mongodb:27017/campusworks_chat
```

### **Production (.env.prod)**
```bash
# Database (External)
DB_HOST=your_production_db_host
DB_PORT=3306
DB_USERNAME=your_production_user
DB_PASSWORD=your_production_password

# Eureka
EUREKA_URL=http://eureka-server:8761/eureka/

# Frontend
VITE_API_BASE_URL=https://your_api_domain.com
VITE_CHAT_SERVICE_URL=https://your_chat_domain.com

# MongoDB (External)
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/campusworks_chat
```

---

## 🚀 **Deployment Commands**

### **Local Development**
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Restart a specific service
docker-compose restart auth-service

# Stop all services
docker-compose down

# Remove all containers and volumes
docker-compose down -v
```

### **Production Deployment**
```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Start production services
docker-compose -f docker-compose.prod.yml up -d

# View production logs
docker-compose -f docker-compose.prod.yml logs -f

# Scale services
docker-compose -f docker-compose.prod.yml up -d --scale auth-service=3
```

---

## 🔍 **Monitoring & Debugging**

### **Health Checks**
```bash
# Check service health
docker-compose ps

# View service logs
docker-compose logs service-name

# Execute commands in container
docker-compose exec service-name bash
```

### **Service URLs**
- **Frontend:** http://localhost:3000
- **API Gateway:** http://localhost:8080
- **Eureka Server:** http://localhost:8761
- **Auth Service:** http://localhost:9000
- **Task Service:** http://localhost:9001
- **Bidding Service:** http://localhost:9002
- **Profile Service:** http://localhost:9003
- **Chat Service:** http://localhost:3001

---

## 🔒 **Security Features**

### **Frontend (Nginx)**
- **Security Headers:** X-Frame-Options, X-Content-Type-Options, X-XSS-Protection
- **CSP:** Content Security Policy
- **Gzip Compression:** Enabled

### **Backend Services**
- **Non-root User:** Chat service runs as non-root
- **Health Checks:** Built-in monitoring
- **Environment Variables:** Secure configuration

---

## 📊 **Performance Optimizations**

### **Multi-stage Builds**
- **Smaller Images:** Only runtime dependencies included
- **Faster Builds:** Cached layers for dependencies
- **Security:** Reduced attack surface

### **Nginx Optimizations**
- **Gzip Compression:** Reduced bandwidth usage
- **Static File Serving:** Optimized for React SPA
- **Caching Headers:** Better performance

---

## 🛠️ **Troubleshooting**

### **Common Issues**

1. **Service won't start:**
   ```bash
   # Check logs
   docker-compose logs service-name
   
   # Check environment variables
   docker-compose config
   ```

2. **Database connection failed:**
   ```bash
   # Check if database is running
   docker-compose ps mysql
   
   # Check database logs
   docker-compose logs mysql
   ```

3. **Port conflicts:**
   ```bash
   # Check port usage
   netstat -tulpn | grep :8080
   
   # Change ports in docker-compose.yml
   ```

### **Useful Commands**
```bash
# Clean up Docker
docker system prune -a

# View resource usage
docker stats

# Inspect container
docker inspect container-name

# View container processes
docker-compose exec service-name ps aux
```

---

## ✅ **Benefits of Docker Deployment**

1. **Consistency** - Same environment everywhere
2. **Scalability** - Easy to scale services
3. **Isolation** - Services run independently
4. **Portability** - Run anywhere Docker is supported
5. **Version Control** - Track infrastructure changes
6. **Easy Rollback** - Quick deployment rollbacks
7. **Resource Management** - Better resource utilization

---

## 🎯 **Next Steps**

1. **Test locally** with `docker-compose up -d`
2. **Configure production** environment variables
3. **Deploy to production** with `docker-compose.prod.yml`
4. **Set up monitoring** and logging
5. **Configure CI/CD** for automated deployments

Your CampusWorks project is now **Docker-ready** and **production-deployable**! 🐳🎉
