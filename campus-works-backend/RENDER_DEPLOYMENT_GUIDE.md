# CampusWorks Backend - Render Deployment Guide

This guide will help you deploy your CampusWorks microservices backend to Render.

## Prerequisites

1. **Render Account**: Sign up at [render.com](https://render.com)
2. **GitHub Repository**: Your code should be pushed to GitHub
3. **MySQL Database**: You'll need a MySQL database (can use Render's managed database)

## Step 1: Set up MySQL Database on Render

1. Go to your Render dashboard
2. Click "New +" → "PostgreSQL" (or MySQL if available)
3. Choose "MySQL" as the database type
4. Configure:
   - **Name**: `campusworks-db`
   - **Database**: `campusworks`
   - **User**: `campusworks_user`
   - **Password**: Generate a strong password
5. Note down the connection details

## Step 2: Create Database Tables

Connect to your MySQL database and run the following SQL to create the required databases:

```sql
-- Create databases
CREATE DATABASE IF NOT EXISTS campusworks_auth;
CREATE DATABASE IF NOT EXISTS campusworks_tasks;
CREATE DATABASE IF NOT EXISTS campusworks_bids;
CREATE DATABASE IF NOT EXISTS campusworks_profile;

-- Grant permissions to your user
GRANT ALL PRIVILEGES ON campusworks_auth.* TO 'campusworks_user'@'%';
GRANT ALL PRIVILEGES ON campusworks_tasks.* TO 'campusworks_user'@'%';
GRANT ALL PRIVILEGES ON campusworks_bids.* TO 'campusworks_user'@'%';
GRANT ALL PRIVILEGES ON campusworks_profile.* TO 'campusworks_user'@'%';

FLUSH PRIVILEGES;
```

## Step 3: Deploy Services to Render

### 3.1 Deploy Eureka Server

1. Go to Render dashboard → "New +" → "Web Service"
2. Connect your GitHub repository
3. Configure:
   - **Name**: `campusworks-eureka`
   - **Root Directory**: `campus-works-backend/eureka-server`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/eureka-server-1.0.0.jar --spring.profiles.active=prod`
   - **Instance Type**: Starter (Free tier)
   - **Port**: `8761`

### 3.2 Deploy Auth Service

1. Create another Web Service
2. Configure:
   - **Name**: `campusworks-auth`
   - **Root Directory**: `campus-works-backend/auth-service`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/auth-service-1.0.0.jar --spring.profiles.active=prod`
   - **Instance Type**: Starter
   - **Port**: `9000`

3. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   AUTH_DB_URL=jdbc:mysql://your-db-host:3306/campusworks_auth?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
   DB_USERNAME=campusworks_user
   DB_PASSWORD=your_db_password
   EUREKA_URL=http://campusworks-eureka:8761/eureka/
   JWT_SECRET=your-super-secure-jwt-secret-key
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ```

### 3.3 Deploy Task Service

1. Create Web Service
2. Configure:
   - **Name**: `campusworks-task`
   - **Root Directory**: `campus-works-backend/task-service`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/task-service-1.0.0.jar --spring.profiles.active=prod`
   - **Instance Type**: Starter
   - **Port**: `9001`

3. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   TASK_DB_URL=jdbc:mysql://your-db-host:3306/campusworks_tasks?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
   DB_USERNAME=campusworks_user
   DB_PASSWORD=your_db_password
   EUREKA_URL=http://campusworks-eureka:8761/eureka/
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ```

### 3.4 Deploy Bidding Service

1. Create Web Service
2. Configure:
   - **Name**: `campusworks-bidding`
   - **Root Directory**: `campus-works-backend/bidding-service`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/bidding-service-1.0.0.jar --spring.profiles.active=prod`
   - **Instance Type**: Starter
   - **Port**: `9002`

3. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   BIDDING_DB_URL=jdbc:mysql://your-db-host:3306/campusworks_bids?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
   DB_USERNAME=campusworks_user
   DB_PASSWORD=your_db_password
   EUREKA_URL=http://campusworks-eureka:8761/eureka/
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ```

### 3.5 Deploy Profile Service

1. Create Web Service
2. Configure:
   - **Name**: `campusworks-profile`
   - **Root Directory**: `campus-works-backend/profile-service`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/profile-service-1.0.0.jar --spring.profiles.active=prod`
   - **Instance Type**: Starter
   - **Port**: `9003`

3. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   PROFILE_DB_URL=jdbc:mysql://your-db-host:3306/campusworks_profile?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
   DB_USERNAME=campusworks_user
   DB_PASSWORD=your_db_password
   EUREKA_URL=http://campusworks-eureka:8761/eureka/
   ```

### 3.6 Deploy API Gateway

1. Create Web Service
2. Configure:
   - **Name**: `campusworks-gateway`
   - **Root Directory**: `campus-works-backend/api-gateway`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/api-gateway-1.0.0.jar --spring.profiles.active=prod`
   - **Instance Type**: Starter
   - **Port**: `8080`

3. **Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   EUREKA_URL=http://campusworks-eureka:8761/eureka/
   JWT_SECRET=your-super-secure-jwt-secret-key
   ```

## Step 4: Update Frontend Configuration

Update your frontend to point to the deployed API Gateway:

```javascript
// In your frontend configuration
const API_BASE_URL = 'https://campusworks-gateway.onrender.com';
```

## Step 5: Service URLs

After deployment, your services will be available at:

- **API Gateway**: `https://campusworks-gateway.onrender.com`
- **Eureka Server**: `https://campusworks-eureka.onrender.com:8761`
- **Auth Service**: `https://campusworks-auth.onrender.com:9000`
- **Task Service**: `https://campusworks-task.onrender.com:9001`
- **Bidding Service**: `https://campusworks-bidding.onrender.com:9002`
- **Profile Service**: `https://campusworks-profile.onrender.com:9003`

## Step 6: Health Checks

Verify all services are running:

```bash
# Check API Gateway
curl https://campusworks-gateway.onrender.com/actuator/health

# Check Eureka Server
curl https://campusworks-eureka.onrender.com:8761/actuator/health

# Check individual services
curl https://campusworks-auth.onrender.com:9000/actuator/health
curl https://campusworks-task.onrender.com:9001/actuator/health
curl https://campusworks-bidding.onrender.com:9002/actuator/health
curl https://campusworks-profile.onrender.com:9003/actuator/health
```

## Troubleshooting

### Common Issues:

1. **Service Discovery Issues**: Ensure Eureka Server is running and accessible
2. **Database Connection**: Verify database credentials and network access
3. **Memory Issues**: Consider upgrading to a paid plan if services crash
4. **Build Failures**: Check Maven dependencies and Java version compatibility

### Logs:

Check service logs in the Render dashboard for detailed error information.

## Cost Optimization

- Start with free tier for development
- Monitor resource usage
- Upgrade to paid plans only when necessary
- Consider using Render's auto-scaling features

## Security Notes

1. Use strong, unique passwords for all services
2. Enable SSL/TLS for all communications
3. Regularly rotate JWT secrets
4. Monitor access logs for suspicious activity
5. Keep dependencies updated

## Support

For issues specific to Render deployment, check:
- [Render Documentation](https://render.com/docs)
- [Render Community](https://community.render.com)
- [CampusWorks Issues](link-to-your-repo-issues)
