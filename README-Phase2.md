# CampusWorks Phase 2 - Core Business Services

## Overview
Phase 2 implements the core business logic of CampusWorks platform, including Task Management, Bidding System, and User Profile Management. All services are built with Spring Boot 3.4.0 and integrate seamlessly through the API Gateway with JWT authentication.

## 🏗️ Architecture

### Services Implemented
1. **Task Service** (Port: 9001) - Task creation, management, and lifecycle
2. **Bidding Service** (Port: 9002) - Bid placement, management, and selection
3. **Profile Service** (Port: 9003) - User profiles, ratings, and work history

### Technology Stack
- **Spring Boot**: 3.4.0
- **Spring Cloud**: Latest stable version
- **Database**: MySQL for all services
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway with JWT security
- **Authentication**: JWT tokens via API Gateway

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- All Phase 1 services running (Eureka Server, API Gateway, Auth Service)

### 1. Database Setup
```bash
# Run the database setup script
mysql -u root -p < setup-phase2-databases.sql
```

### 2. Start All Services
```bash
# Use the provided batch script
start-phase2.bat

# Or start manually in order:
# 1. Eureka Server (Port 8761)
# 2. API Gateway (Port 8080)
# 3. Auth Service (Port 9000)
# 4. Task Service (Port 9001)
# 5. Bidding Service (Port 9002)
# 6. Profile Service (Port 9003)
```

### 3. Verify Services
```bash
# Check Eureka Dashboard
http://localhost:8761

# Test API Gateway
http://localhost:8080/actuator/health

# Test individual services
http://localhost:9001/actuator/health
http://localhost:9002/actuator/health
http://localhost:9003/actuator/health
```

## 📋 Service Details

### Task Service
**Port**: 9001  
**Database**: `campusworks_tasks`

#### Key Features
- Task CRUD operations
- Task status management (OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED)
- Category-based organization
- Bidding deadline management
- Task assignment and completion tracking

#### Main Endpoints
- `POST /tasks` - Create new task
- `GET /tasks` - Get all tasks
- `GET /tasks/{id}` - Get task by ID
- `PUT /tasks/{id}` - Update task
- `DELETE /tasks/{id}` - Delete task
- `PUT /tasks/{id}/assign` - Assign task to user
- `PUT /tasks/{id}/complete` - Mark task as completed

### Bidding Service
**Port**: 9002  
**Database**: `campusworks_bids`

#### Key Features
- Bid placement and management
- Bid status tracking (PENDING, ACCEPTED, REJECTED, WITHDRAWN)
- Winning bid selection
- Bid proposal management
- User bid history

#### Main Endpoints
- `POST /bids` - Place new bid
- `GET /bids` - Get all bids
- `GET /bids/task/{taskId}` - Get bids for specific task
- `PUT /bids/{id}/accept` - Accept bid
- `PUT /bids/{id}/reject` - Reject bid
- `PUT /bids/{id}/withdraw` - Withdraw bid

### Profile Service
**Port**: 9003  
**Database**: `campusworks_profile`

#### Key Features
- User profile management
- Skills and experience tracking
- Rating and review system
- Work history and earnings
- Availability status management

#### Main Endpoints
- `POST /profiles` - Create user profile
- `GET /profiles/public` - Get all public profiles
- `GET /profiles/{id}` - Get profile by ID
- `PUT /profiles/{id}` - Update profile
- `PUT /profiles/{id}/rating` - Add rating to profile
- `PUT /profiles/{id}/task-completed` - Mark task as completed

## 🔐 Authentication & Security

### JWT Flow
1. User registers/logs in via Auth Service
2. Auth Service returns JWT token
3. API Gateway validates JWT for all subsequent requests
4. Gateway adds user context headers (`X-User-Id`, `X-User-Email`, `X-User-Roles`)
5. Downstream services use these headers for authorization

### Required Headers
For authenticated endpoints, include:
```
Authorization: Bearer <JWT_TOKEN>
X-User-Id: <USER_ID>
X-User-Email: <USER_EMAIL>
```

## 🗄️ Database Schema

### Task Service Database
```sql
CREATE DATABASE campusworks_tasks;
-- Tables: tasks (with indexes for performance)
```

### Bidding Service Database
```sql
CREATE DATABASE campusworks_bids;
-- Tables: bids (with indexes for performance)
```

### Profile Service Database
```sql
CREATE DATABASE campusworks_profile;
-- Tables: profiles (with indexes for performance)
```

## 🧪 Testing

### Automated Testing
```bash
# Run the comprehensive test script
test-phase2.bat
```

### Manual Testing with Postman
1. Import `CampusWorks-Phase2-Postman-Collection.json`
2. Set environment variables:
   - `base_url`: `http://localhost:8080`
   - `jwt_token`: (leave empty initially)
   - `user_id`: `1`
   - `user_email`: `student@campusworks.com`

### Test Flow
1. **Register/Login**: Get JWT token
2. **Create Profile**: Set up user profile
3. **Create Task**: Post a task for bidding
4. **Place Bids**: Test bidding functionality
5. **Manage Tasks**: Test task lifecycle

## 📊 API Gateway Routes

### Direct Routes
- `/tasks/**` → Task Service
- `/bids/**` → Bidding Service
- `/profiles/**` → Profile Service

### API Routes
- `/api/tasks/**` → Task Service (with StripPrefix=1)
- `/api/bids/**` → Bidding Service (with StripPrefix=1)
- `/api/profiles/**` → Profile Service (with StripPrefix=1)

## 🔍 Monitoring & Health Checks

### Actuator Endpoints
All services expose health check endpoints:
- `/actuator/health` - Service health status
- `/actuator/info` - Service information

### Logging
Comprehensive logging implemented across all services:
- Request/response logging
- Business logic logging
- Error logging with stack traces
- Performance metrics

## 🚨 Troubleshooting

### Common Issues

#### 1. Service Not Starting
- Check if MySQL is running
- Verify database exists and tables are created
- Check port availability
- Review service logs for errors

#### 2. Database Connection Issues
- Verify MySQL credentials in `application.properties`
- Ensure database exists: `campusworks_tasks`, `campusworks_bids`, `campusworks_profile`
- Check MySQL service status

#### 3. JWT Authentication Issues
- Verify JWT token is valid and not expired
- Check if `X-User-Id` and `X-User-Email` headers are set
- Ensure API Gateway is running and JWT secret is configured

#### 4. Service Discovery Issues
- Check Eureka Server is running on port 8761
- Verify all services are registered in Eureka Dashboard
- Check service names match exactly in `application.properties`

### Debug Commands
```bash
# Check service health
curl http://localhost:9001/actuator/health
curl http://localhost:9002/actuator/health
curl http://localhost:9003/actuator/health

# Check Eureka registration
curl http://localhost:8761/eureka/apps

# Test API Gateway routing
curl http://localhost:8080/tasks
curl http://localhost:8080/bids
curl http://localhost:8080/profiles
```

## 📈 Performance Considerations

### Database Optimization
- Indexes on frequently queried fields
- Proper table relationships
- Query optimization

### Service Optimization
- Connection pooling
- Caching strategies
- Async processing where appropriate

## 🔄 Next Steps (Phase 3)

Phase 3 will implement:
- **Payment Service** - Payment processing and escrow
- **Notification Service** - Real-time notifications
- **Chat Service** - User communication system
- **File Upload Service** - Document and file management

## 📚 Additional Resources

- [Phase 1 Documentation](README-Phase1.md)
- [Project Blueprint](CampusWorks_Project_Blueprint.md)
- [API Documentation](CampusWorks-Phase2-Postman-Collection.json)
- [Database Schema](setup-phase2-databases.sql)

## 🤝 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review service logs for error details
3. Verify all prerequisites are met
4. Ensure services are started in correct order

---

**Phase 2 Status**: ✅ Complete  
**Next Phase**: Phase 3 - Payment & Communication Services
