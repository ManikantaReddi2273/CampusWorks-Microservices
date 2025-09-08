-- Database initialization script for CampusWorks microservices
-- This script creates all required databases for the microservices

-- Create databases
CREATE DATABASE IF NOT EXISTS campusworks_auth;
CREATE DATABASE IF NOT EXISTS campusworks_tasks;
CREATE DATABASE IF NOT EXISTS campusworks_bids;
CREATE DATABASE IF NOT EXISTS campusworks_profile;

-- Grant permissions
GRANT ALL PRIVILEGES ON campusworks_auth.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON campusworks_tasks.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON campusworks_bids.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON campusworks_profile.* TO 'root'@'%';

-- Flush privileges
FLUSH PRIVILEGES;
