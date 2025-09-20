# CampusWorks - Peer-to-Peer Academic Task Outsourcing Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.1.1-blue.svg)](https://reactjs.org/)
[![Node.js](https://img.shields.io/badge/Node.js-18+-green.svg)](https://nodejs.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0+-green.svg)](https://www.mongodb.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

CampusWorks is a peer-to-peer academic task outsourcing platform where students can post tasks they need help with and other students can bid on them. The platform follows a simplified role system where all users are STUDENTs by default, and the same person can both post tasks AND bid on tasks.

### Core Workflow:
1. **Student posts task** with budget and requirements
2. **Other students bid** on tasks within time window
3. **Lowest bidder wins** and gets assigned
4. **Assigned student completes work** offline and notifies completion
5. **Task owner verifies work** offline and accepts online
6. **Payment processed** through UPI ID exchange
7. **Chat communication** for task clarification

## ✨ Features

### 🔐 Authentication & Authorization
- User registration with email verification
- JWT-based authentication
- Simplified role system (STUDENT/ADMIN)
- Password reset functionality

### 📝 Task Management
- Create, view, edit, and manage tasks
- Task categories (Academic Writing, Programming, Mathematics, Science, etc.)
- Budget and deadline management
- Task status tracking (OPEN → IN_PROGRESS → COMPLETED → ACCEPTED)

### 🏷️ Bidding System
- Place bids with proposals
- Automatic bid selection (lowest bidder wins)
- Real-time countdown timers
- Bid status tracking and management

### 💬 Real-time Communication
- Socket.io-based chat system
- Task-based chat rooms
- Real-time messaging
- Typing indicators and read status

### 👤 Profile Management
- User profile creation and editing
- Profile visibility settings
- Task and bid history tracking

## 🏗️ Architecture

### Microservices Architecture
- **Eureka Server** (Port: 8761) - Service discovery and registration
- **API Gateway** (Port: 8080) - Centralized routing, JWT validation, CORS
- **Auth Service** (Port: 9000) - User authentication and JWT generation
- **Task Service** (Port: 9001) - Task CRUD operations and status management
- **Bidding Service** (Port: 9002) - Bid management and auction logic
- **Profile Service** (Port: 9006) - User profile management
- **Chat Service** (Port: 3001) - Real-time messaging with Socket.io

### Frontend
- **React Application** (Port: 3000) - Modern UI with Material-UI components

## 🛠️ Technology Stack

### Backend Services
- **Spring Boot 3.4.0** with Java 17
- **Spring Cloud Gateway** for API routing
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **Feign Clients** for inter-service communication
- **Eureka** for service discovery

### Frontend
- **React 19.1.1** with functional components and hooks
- **Redux Toolkit** for state management
- **Material-UI 7.3.1** for UI components
- **React Router v7.8.2** for navigation
- **Axios** for API communication
- **Socket.io-client** for real-time features

### Chat Service
- **Node.js 18+** with Express
- **Socket.io 4.7.4** for real-time communication
- **MongoDB** with Mongoose for data persistence

### Databases
- **MySQL 8.0** for relational data (users, tasks, bids, profiles)
- **MongoDB 6.0** for chat messages and real-time data

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17** or higher
- **Node.js 18+** and npm
- **MySQL 8.0+**
- **MongoDB 6.0+**
- **Maven 3.8+**
- **Git**

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/CampusWorks-Microservices.git
cd CampusWorks-Microservices
```

### 2. Backend Setup
```bash
# Navigate to backend directory
cd campus-works-backend

# Build all services
mvn clean install

# Start services in order (use separate terminals):
# 1. Start Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Start API Gateway
cd ../api-gateway
mvn spring-boot:run

# 3. Start Auth Service
cd ../auth-service
mvn spring-boot:run

# 4. Start Task Service
cd ../task-service
mvn spring-boot:run

# 5. Start Bidding Service
cd ../bidding-service
mvn spring-boot:run

# 6. Start Profile Service
cd ../profile-service
mvn spring-boot:run
```

### 3. Chat Service Setup
```bash
# Navigate to chat service directory
cd campusworks-chat-service

# Install dependencies
npm install

# Start chat service
npm start
```

### 4. Frontend Setup
```bash
# Navigate to frontend directory
cd campus-works-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

## ⚙️ Configuration

### Database Configuration
Create the following MySQL databases:
```sql
CREATE DATABASE campusworks_auth;
CREATE DATABASE campusworks_tasks;
CREATE DATABASE campusworks_bids;
CREATE DATABASE campusworks_profile;
```

### Environment Variables
Update the following configuration files:

**Backend Services** (`application.properties`):
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/campusworks_auth
spring.datasource.username=root
spring.datasource.password=your_password

# JWT Configuration
security.jwt.secret=mysupersecuresecretkeythatismorethan32chars
security.jwt.expiration=86400000

# Email Configuration (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

**Chat Service** (`.env`):
```env
PORT=3001
NODE_ENV=development
SPRING_BOOT_BASE_URL=http://localhost:8080
JWT_SECRET=mysupersecuresecretkeythatismorethan32chars
MONGODB_URI=mongodb://localhost:27017/campusworks_chat
SOCKET_CORS_ORIGIN=http://localhost:3000
```

**Frontend** (`.env.development`):
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=CampusWorks
VITE_APP_VERSION=1.0.0
```

## 🏃‍♂️ Running the Application

### Start Order
1. **MySQL** and **MongoDB** databases
2. **Eureka Server** (Port: 8761)
3. **API Gateway** (Port: 8080)
4. **Auth Service** (Port: 9000)
5. **Task Service** (Port: 9001)
6. **Bidding Service** (Port: 9002)
7. **Profile Service** (Port: 9006)
8. **Chat Service** (Port: 3001)
9. **Frontend** (Port: 3000)

### Access Points
- **Frontend Application**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Chat Service Health**: http://localhost:3001/health

### Default Admin Credentials
- **Email**: admin@campusworks.com
- **Password**: admin123

## 📚 API Documentation

### Authentication Endpoints
```
POST /api/auth/register - User registration
POST /api/auth/login - User login
POST /api/auth/verify-email - Email verification
POST /api/auth/reset-password - Password reset
```

### Task Endpoints
```
GET /api/tasks - Get all tasks
POST /api/tasks - Create new task
GET /api/tasks/{id} - Get task by ID
PUT /api/tasks/{id} - Update task
DELETE /api/tasks/{id} - Delete task
GET /api/tasks/my-tasks - Get user's tasks
```

### Bidding Endpoints
```
GET /api/bids/task/{taskId} - Get bids for task
POST /api/bids - Place new bid
GET /api/bids/my-bids - Get user's bids
DELETE /api/bids/{id} - Withdraw bid
```

### Profile Endpoints
```
GET /api/profiles - Get all profiles
POST /api/profiles - Create profile
GET /api/profiles/{id} - Get profile by ID
PUT /api/profiles/{id} - Update profile
```

### Chat Endpoints
```
GET /api/chat/rooms - Get user's chat rooms
POST /api/chat/rooms/task/{taskId} - Create/get chat room
GET /api/chat/rooms/{roomId}/messages - Get messages
POST /api/chat/rooms/{roomId}/messages/read - Mark as read
```

## 🗄️ Database Schema

### MySQL Tables

**Users Table** (campusworks_auth)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') NOT NULL,
    enabled BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Tasks Table** (campusworks_tasks)
```sql
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    budget DECIMAL(10,2) NOT NULL,
    category ENUM('ACADEMIC_WRITING', 'PROGRAMMING', 'MATHEMATICS', 'SCIENCE', 'LITERATURE', 'ENGINEERING', 'OTHER') NOT NULL,
    status ENUM('OPEN', 'IN_PROGRESS', 'COMPLETED', 'ACCEPTED', 'CANCELLED') DEFAULT 'OPEN',
    owner_id BIGINT NOT NULL,
    assigned_user_id BIGINT,
    bidding_deadline TIMESTAMP NOT NULL,
    completion_deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Bids Table** (campusworks_bids)
```sql
CREATE TABLE bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    proposal TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') DEFAULT 'PENDING',
    is_winning BOOLEAN DEFAULT FALSE,
    upi_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### MongoDB Collections

**Chat Rooms Collection**
```javascript
{
    taskId: String,
    taskTitle: String,
    ownerId: String,
    ownerEmail: String,
    bidderId: String,
    bidderEmail: String,
    status: String,
    lastMessageAt: Date,
    unreadCount: Object
}
```

**Messages Collection**
```javascript
{
    roomId: String,
    taskId: String,
    senderId: String,
    senderEmail: String,
    message: String,
    messageType: String,
    isRead: Boolean,
    createdAt: Date
}
```

## 📁 Project Structure

```
CampusWorks-Microservices/
├── campus-works-backend/
│   ├── eureka-server/          # Service discovery
│   ├── api-gateway/            # API Gateway with JWT validation
│   ├── auth-service/           # Authentication service
│   ├── task-service/           # Task management service
│   ├── bidding-service/        # Bidding and auction service
│   └── profile-service/        # Profile management service
├── campus-works-frontend/
│   ├── src/
│   │   ├── components/         # React components (atoms, molecules, organisms)
│   │   ├── pages/              # Page components
│   │   ├── store/              # Redux store and slices
│   │   ├── services/           # API services
│   │   ├── utils/              # Utility functions
│   │   └── theme/              # Material-UI theme
│   └── public/                 # Static assets
└── campusworks-chat-service/
    ├── src/
    │   ├── controllers/        # API controllers
    │   ├── models/             # MongoDB models
    │   ├── routes/             # Express routes
    │   ├── socket/             # Socket.io handlers
    │   └── services/           # External service integrations
    └── logs/                   # Application logs
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Backend Development**: Spring Boot Microservices
- **Frontend Development**: React.js with Material-UI
- **Chat Service**: Node.js with Socket.io
- **Database Design**: MySQL and MongoDB

## 📞 Support

For support and questions, please open an issue in the GitHub repository or contact the development team.

---

**CampusWorks** - Connecting students through peer-to-peer academic collaboration.
