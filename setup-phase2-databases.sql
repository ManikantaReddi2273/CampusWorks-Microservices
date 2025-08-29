-- =============================================
-- CampusWorks Phase 2 Database Setup Script
-- =============================================
-- This script creates the databases for Task, Bidding, and Profile services
-- Run this script in MySQL to set up the required databases

-- =============================================
-- Task Service Database
-- =============================================
CREATE DATABASE IF NOT EXISTS campusworks_tasks;
USE campusworks_tasks;

-- Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    budget DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    owner_id BIGINT NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    assigned_user_id BIGINT,
    assigned_user_email VARCHAR(255),
    bidding_deadline DATETIME NOT NULL,
    completion_deadline DATETIME NOT NULL,
    completed_at DATETIME,
    accepted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_owner_id (owner_id),
    INDEX idx_assigned_user_id (assigned_user_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_bidding_deadline (bidding_deadline),
    INDEX idx_completion_deadline (completion_deadline)
);

-- =============================================
-- Bidding Service Database
-- =============================================
CREATE DATABASE IF NOT EXISTS campusworks_bids;
USE campusworks_bids;

-- Bids table
CREATE TABLE IF NOT EXISTS bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    bidder_email VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    proposal TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_winning BOOLEAN DEFAULT FALSE,
    is_accepted BOOLEAN DEFAULT FALSE,
    accepted_at DATETIME,
    rejected_at DATETIME,
    rejection_reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_task_id (task_id),
    INDEX idx_bidder_id (bidder_id),
    INDEX idx_status (status),
    INDEX idx_is_winning (is_winning),
    INDEX idx_amount (amount)
);

-- =============================================
-- Profile Service Database
-- =============================================
CREATE DATABASE IF NOT EXISTS campusworks_profile;
USE campusworks_profile;

-- Profiles table
CREATE TABLE IF NOT EXISTS profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    bio TEXT,
    university VARCHAR(255),
    major VARCHAR(255),
    academic_year VARCHAR(50),
    skills TEXT,
    experience_years INT DEFAULT 0,
    experience_description TEXT,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_ratings INT DEFAULT 0,
    completed_tasks INT DEFAULT 0,
    successful_tasks INT DEFAULT 0,
    total_earnings DECIMAL(10,2) DEFAULT 0.00,
    is_verified BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT TRUE,
    preferred_categories TEXT,
    hourly_rate DECIMAL(8,2),
    availability_status VARCHAR(50) DEFAULT 'AVAILABLE',
    last_active DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_user_email (user_email),
    INDEX idx_university (university),
    INDEX idx_major (major),
    INDEX idx_rating (rating),
    INDEX idx_availability_status (availability_status),
    INDEX idx_is_verified (is_verified),
    INDEX idx_is_public (is_public)
);

-- =============================================
-- Verification
-- =============================================
SELECT 'Task Service Database' as Service, COUNT(*) as Tables FROM information_schema.tables WHERE table_schema = 'campusworks_tasks'
UNION ALL
SELECT 'Bidding Service Database' as Service, COUNT(*) as Tables FROM information_schema.tables WHERE table_schema = 'campusworks_bids'
UNION ALL
SELECT 'Profile Service Database' as Service, COUNT(*) as Tables FROM information_schema.tables WHERE table_schema = 'campusworks_profile';

-- =============================================
-- Sample Data (Optional - for testing)
-- =============================================
-- Uncomment the following lines to insert sample data for testing

/*
USE campusworks_tasks;
INSERT INTO tasks (title, description, budget, category, owner_id, owner_email, bidding_deadline, completion_deadline) VALUES
('Math Assignment Help', 'Need help with calculus problems', 50.00, 'MATHEMATICS', 1, 'student@example.com', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY)),
('Programming Project', 'Java project implementation', 100.00, 'COMPUTER_SCIENCE', 2, 'student2@example.com', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 12 DAY));

USE campusworks_bids;
INSERT INTO bids (task_id, bidder_id, bidder_email, amount, proposal) VALUES
(1, 3, 'worker@example.com', 45.00, 'I can help with calculus problems'),
(1, 4, 'worker2@example.com', 40.00, 'Experienced in mathematics'),
(2, 3, 'worker@example.com', 90.00, 'Java expert available');

USE campusworks_profile;
INSERT INTO profiles (user_id, user_email, first_name, last_name, university, major, rating, total_ratings) VALUES
(3, 'worker@example.com', 'John', 'Worker', 'Tech University', 'Computer Science', 4.5, 10),
(4, 'worker2@example.com', 'Jane', 'Helper', 'Math University', 'Mathematics', 4.8, 15);
*/
