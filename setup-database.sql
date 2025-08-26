-- ========================================
-- CampusWorks Phase 1 Database Setup
-- ========================================
-- This script sets up the MySQL database for Phase 1 services
-- Run this script in MySQL to create the required database

-- Create the auth service database
CREATE DATABASE IF NOT EXISTS campusworks_auth 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Use the auth database
USE campusworks_auth;

-- Create users table (will be auto-created by JPA, but here for reference)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default admin user (will be created automatically by the service, but here for reference)
-- INSERT INTO users (email, password, role, enabled) VALUES 
-- ('admin@campusworks.com', '$2a$10$encrypted_password_here', 'ADMIN', true);

-- Show the created database
SHOW DATABASES LIKE 'campusworks_%';

-- Show tables in auth database
USE campusworks_auth;
SHOW TABLES;

-- Display database information
SELECT 
    'campusworks_auth' as database_name,
    'Phase 1 - Authentication Service' as description,
    'MySQL 8.0' as database_type,
    'Ready for Spring Boot JPA' as status;
