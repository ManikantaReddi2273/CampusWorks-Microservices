# CampusWorks Backend - Docker Setup

This directory contains Docker configurations for deploying the CampusWorks microservices backend.

## 🏗️ Architecture

The backend consists of 6 microservices:

1. **Eureka Server** (Port 8761) - Service discovery and registry
2. **API Gateway** (Port 8080) - Entry point for all API requests
3. **Auth Service** (Port 9000) - Authentication and authorization
4. **Task Service** (Port 9001) - Task management
5. **Bidding Service** (Port 9002) - Bidding system
6. **Profile Service** (Port 9003) - User profiles

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose installed
- At least 4GB RAM available
- Ports 3306, 8080, 8761, 9000-9003 available

### Local Development

1. **Clone and navigate to backend directory:**
   ```bash
   cd campus-works-backend
   ```

2. **Start all services:**
   ```bash
   docker-compose up -d
   ```

3. **Check service health:**
   ```bash
   # API Gateway
   curl http://localhost:8080/actuator/health
   
   # Eureka Server
   curl http://localhost:8761/actuator/health
   
   # Individual services
   curl http://localhost:9000/actuator/health  # Auth
   curl http://localhost:9001/actuator/health  # Task
   curl http://localhost:9002/actuator/health  # Bidding
   curl http://localhost:9003/actuator/health  # Profile
   ```

4. **View Eureka Dashboard:**
   Open http://localhost:8761 in your browser

### Production Deployment

1. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your production values
   ```

2. **Deploy with production configuration:**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

## 📁 File Structure

```
campus-works-backend/
├── api-gateway/
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties
│       └── application-prod.properties
├── auth-service/
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties
│       └── application-prod.properties
├── task-service/
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties
│       └── application-prod.properties
├── bidding-service/
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties
│       └── application-prod.properties
├── profile-service/
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties
│       └── application-prod.properties
├── eureka-server/
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties
│       └── application-prod.properties
├── docker-compose.yml          # Local development
├── docker-compose.prod.yml     # Production deployment
├── init-databases.sql          # Database initialization
├── .env.example               # Environment variables template
├── .dockerignore              # Docker ignore file
├── build-all.sh               # Build script
└── RENDER_DEPLOYMENT_GUIDE.md # Render deployment guide
```

## 🔧 Configuration

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
# Database Configuration
AUTH_DB_URL=jdbc:mysql://mysql:3306/campusworks_auth?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
TASK_DB_URL=jdbc:mysql://mysql:3306/campusworks_tasks?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
BIDDING_DB_URL=jdbc:mysql://mysql:3306/campusworks_bids?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
PROFILE_DB_URL=jdbc:mysql://mysql:3306/campusworks_profile?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true

# Database Credentials
DB_USERNAME=root
DB_PASSWORD=reddi2273

# Eureka Configuration
EUREKA_URL=http://eureka-server:8761/eureka/
EUREKA_HOSTNAME=eureka-server

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Service Ports

| Service | Port | Description |
|---------|------|-------------|
| MySQL | 3306 | Database |
| Eureka Server | 8761 | Service Discovery |
| API Gateway | 8080 | Main Entry Point |
| Auth Service | 9000 | Authentication |
| Task Service | 9001 | Task Management |
| Bidding Service | 9002 | Bidding System |
| Profile Service | 9003 | User Profiles |

## 🛠️ Development Commands

### Build Individual Service

```bash
# Build specific service
cd auth-service
docker build -t campusworks-auth:latest .

# Run specific service
docker run -p 9000:9000 campusworks-auth:latest
```

### Build All Services

```bash
# Make script executable
chmod +x build-all.sh

# Build all services
./build-all.sh
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
```

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## 🐛 Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using the port
   lsof -i :8080
   
   # Kill the process
   kill -9 <PID>
   ```

2. **Database Connection Issues**
   ```bash
   # Check MySQL container
   docker-compose logs mysql
   
   # Connect to MySQL
   docker-compose exec mysql mysql -u root -p
   ```

3. **Service Discovery Issues**
   ```bash
   # Check Eureka Server
   curl http://localhost:8761/eureka/apps
   
   # Check service logs
   docker-compose logs eureka-server
   ```

4. **Memory Issues**
   ```bash
   # Check Docker memory usage
   docker stats
   
   # Increase Docker memory limit in Docker Desktop
   ```

### Health Checks

All services include health check endpoints:

- **API Gateway**: http://localhost:8080/actuator/health
- **Eureka Server**: http://localhost:8761/actuator/health
- **Auth Service**: http://localhost:9000/actuator/health
- **Task Service**: http://localhost:9001/actuator/health
- **Bidding Service**: http://localhost:9002/actuator/health
- **Profile Service**: http://localhost:9003/actuator/health

## 🚀 Deployment

### Render Deployment

See [RENDER_DEPLOYMENT_GUIDE.md](RENDER_DEPLOYMENT_GUIDE.md) for detailed instructions on deploying to Render.

### Other Cloud Providers

The Docker setup can be adapted for other cloud providers:

- **AWS ECS**: Use ECS task definitions
- **Google Cloud Run**: Use Cloud Run services
- **Azure Container Instances**: Use ACI containers
- **Kubernetes**: Use Kubernetes manifests

## 📊 Monitoring

### Service Discovery

- **Eureka Dashboard**: http://localhost:8761
- **Service Registry**: http://localhost:8761/eureka/apps

### Application Metrics

- **Actuator Endpoints**: `/actuator/health`, `/actuator/info`
- **JVM Metrics**: Available via Actuator
- **Custom Metrics**: Service-specific metrics

## 🔒 Security

### Production Considerations

1. **Use strong passwords** for all services
2. **Enable SSL/TLS** for all communications
3. **Rotate JWT secrets** regularly
4. **Monitor access logs** for suspicious activity
5. **Keep dependencies updated**

### Network Security

- Services communicate through Docker network
- External access only through API Gateway
- Database not exposed externally in production

## 📝 Logs

### Log Levels

- **Development**: DEBUG level for detailed logging
- **Production**: INFO level for performance

### Log Locations

- **Container Logs**: `docker-compose logs <service>`
- **Application Logs**: Available via Actuator endpoints
- **Database Logs**: `docker-compose logs mysql`

## 🤝 Contributing

1. Make changes to service code
2. Test locally with Docker
3. Update documentation if needed
4. Submit pull request

## 📞 Support

For issues with Docker setup:

1. Check this README
2. Check service logs
3. Check health endpoints
4. Create an issue in the repository

---

**Happy Coding! 🎉**
