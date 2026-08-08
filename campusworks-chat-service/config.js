const fs = require('fs');
const path = require('path');

// Prefer Render/process env; fall back to local config.env if present
const envPath = path.join(__dirname, 'config.env');
if (fs.existsSync(envPath)) {
  require('dotenv').config({ path: envPath });
} else {
  require('dotenv').config();
}

module.exports = {
  // Server Configuration
  port: process.env.PORT || 3001,
  nodeEnv: process.env.NODE_ENV || 'development',

  // Spring Boot Integration
  springBootBaseUrl: process.env.SPRING_BOOT_BASE_URL || 'http://localhost:8080',
  authServiceUrl: process.env.AUTH_SERVICE_URL || 'http://localhost:9000',
  taskServiceUrl: process.env.TASK_SERVICE_URL || 'http://localhost:9001',
  biddingServiceUrl: process.env.BIDDING_SERVICE_URL || 'http://localhost:9002',

  // JWT Configuration (must match Spring JWT_SECRET)
  jwtSecret: process.env.JWT_SECRET || 'mysupersecuresecretkeythatismorethan32chars',
  jwtIssuer: process.env.JWT_ISSUER || 'campusworks',

  // Database Configuration (MongoDB Atlas in production)
  mongodbUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/campusworks_chat',
  redisUrl: process.env.REDIS_URL || '',

  // Socket.io Configuration (comma-separated origins supported by socket handler / cors)
  socketCorsOrigin: process.env.SOCKET_CORS_ORIGIN || 'http://localhost:3000',
  socketMaxConnections: parseInt(process.env.SOCKET_MAX_CONNECTIONS, 10) || 1000,

  // Logging
  logLevel: process.env.LOG_LEVEL || 'info'
};
