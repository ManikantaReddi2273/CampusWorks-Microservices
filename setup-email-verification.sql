-- ===============================================
-- CampusWorks - Email Verification Setup
-- ===============================================
-- This script creates the necessary tables and updates for email verification functionality

USE campusworks_auth;

-- ===============================================
-- 1. Update users table with email verification fields
-- ===============================================
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Update existing users to have enabled=false (they need to verify email)
-- Comment out the next line if you want to keep existing users enabled
-- UPDATE users SET enabled = FALSE WHERE email_verified = FALSE;

-- ===============================================
-- 2. Create verification_tokens table
-- ===============================================
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(500) NOT NULL UNIQUE COMMENT 'Unique verification token',
    user_id BIGINT NOT NULL COMMENT 'Reference to user who owns this token',
    expiry_date TIMESTAMP NOT NULL COMMENT 'When this token expires',
    used BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether token has been used',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When token was created',
    used_at TIMESTAMP NULL COMMENT 'When token was used',
    token_type ENUM('EMAIL_VERIFICATION', 'PASSWORD_RESET') NOT NULL DEFAULT 'EMAIL_VERIFICATION' COMMENT 'Type of verification token',
    
    -- Foreign key constraint
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_token_type (token_type),
    INDEX idx_used (used),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Stores email verification and password reset tokens';

-- ===============================================
-- 3. Create indexes for better performance
-- ===============================================
-- Index on users.email_verified for quick filtering
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON users(email_verified);

-- Composite index for finding unverified users
CREATE INDEX IF NOT EXISTS idx_users_enabled_verified ON users(enabled, email_verified);

-- Index on users.created_at for sorting
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- ===============================================
-- 4. Sample data for testing (optional)
-- ===============================================
-- Insert a test admin user (already verified)
INSERT IGNORE INTO users (email, password, role, enabled, email_verified, email_verified_at) 
VALUES (
    'admin@rguktn.ac.in', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: 'password'
    'ADMIN', 
    TRUE, 
    TRUE, 
    NOW()
);

-- ===============================================
-- 5. Show table structures
-- ===============================================
DESCRIBE users;
DESCRIBE verification_tokens;

-- ===============================================
-- 6. Show success message
-- ===============================================
SELECT 'Email verification tables created successfully!' as Status;
SELECT 'Remember to configure SMTP settings in application.properties' as Reminder;

-- ===============================================
-- 7. Useful queries for monitoring
-- ===============================================
-- Count users by verification status
SELECT 
    'Total Users' as Status, COUNT(*) as Count 
FROM users
UNION ALL
SELECT 
    'Verified Users' as Status, COUNT(*) as Count 
FROM users WHERE email_verified = TRUE
UNION ALL
SELECT 
    'Unverified Users' as Status, COUNT(*) as Count 
FROM users WHERE email_verified = FALSE
UNION ALL
SELECT 
    'Active Tokens' as Status, COUNT(*) as Count 
FROM verification_tokens WHERE used = FALSE AND expiry_date > NOW();
