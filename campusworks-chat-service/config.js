// Load environment variables from .env file
require('dotenv').config();

module.exports = {
  // Server Configuration
  port: process.env.PORT || 3001,
  host: process.env.HOST || '0.0.0.0',
  nodeEnv: process.env.NODE_ENV || 'development',

  // Spring Boot Integration
  springBootBaseUrl: process.env.SPRING_BOOT_API_URL || 'http://localhost:8080',
  authServiceUrl: process.env.AUTH_SERVICE_URL || 'http://localhost:9000',
  taskServiceUrl: process.env.TASK_SERVICE_URL || 'http://localhost:9001',
  biddingServiceUrl: process.env.BIDDING_SERVICE_URL || 'http://localhost:9002',

  // JWT Configuration
  jwtSecret: process.env.JWT_SECRET || 'your_jwt_secret_same_as_spring_boot',
  jwtIssuer: process.env.JWT_ISSUER || 'campusworks',
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || '24h',

  // Database Configuration
  mongodbUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/campusworks_chat',
  redisUrl: process.env.REDIS_URL || 'redis://localhost:6379',

  // Socket.io Configuration
  socketCorsOrigin: process.env.CORS_ORIGIN || 'http://localhost:3000',
  socketMaxConnections: parseInt(process.env.SOCKET_MAX_CONNECTIONS) || 1000,

  // Logging
  logLevel: process.env.LOG_LEVEL || 'info'
};