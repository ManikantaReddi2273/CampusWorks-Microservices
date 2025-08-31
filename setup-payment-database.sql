-- ===================================
-- CAMPUSWORKS PAYMENT SERVICE DATABASE SETUP
-- ===================================

-- Create database for Payment Service
CREATE DATABASE IF NOT EXISTS campusworks_payment;
USE campusworks_payment;

-- ===================================
-- PAYMENTS TABLE
-- ===================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    client_user_id BIGINT NOT NULL,
    worker_user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) NOT NULL,
    worker_amount DECIMAL(10,2) NOT NULL,
    status ENUM('CREATED', 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL,
    payment_method ENUM('RAZORPAY_CARD', 'RAZORPAY_UPI', 'RAZORPAY_NET_BANKING', 'RAZORPAY_WALLET', 'WALLET_BALANCE') NOT NULL,
    razorpay_order_id VARCHAR(100) UNIQUE,
    razorpay_payment_id VARCHAR(100) UNIQUE,
    razorpay_signature VARCHAR(500),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    description VARCHAR(500),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    failed_at TIMESTAMP NULL,
    
    INDEX idx_task_id (task_id),
    INDEX idx_client_user_id (client_user_id),
    INDEX idx_worker_user_id (worker_user_id),
    INDEX idx_status (status),
    INDEX idx_razorpay_order_id (razorpay_order_id),
    INDEX idx_razorpay_payment_id (razorpay_payment_id),
    INDEX idx_created_at (created_at)
);

-- ===================================
-- ESCROWS TABLE
-- ===================================
CREATE TABLE IF NOT EXISTS escrows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL UNIQUE,
    payment_id BIGINT NOT NULL,
    client_user_id BIGINT NOT NULL,
    worker_user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2) NOT NULL,
    worker_amount DECIMAL(10,2) NOT NULL,
    status ENUM('CREATED', 'FUNDED', 'RELEASED', 'REFUNDED', 'DISPUTED') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL, -- 🔥 KEY: Tied to task deadline
    released_at TIMESTAMP NULL,
    refunded_at TIMESTAMP NULL,
    release_reason VARCHAR(500),
    refund_reason VARCHAR(500),
    
    INDEX idx_task_id (task_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_client_user_id (client_user_id),
    INDEX idx_worker_user_id (worker_user_id),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at),
    INDEX idx_status_expires (status, expires_at),
    
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE RESTRICT
);

-- ===================================
-- TRANSACTIONS TABLE
-- ===================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    task_id BIGINT,
    payment_id BIGINT,
    escrow_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    type ENUM('PAYMENT_RECEIVED', 'ESCROW_FUNDED', 'PAYMENT_RELEASED', 'PAYMENT_REFUNDED', 'PLATFORM_FEE', 'WALLET_DEPOSIT', 'WALLET_WITHDRAWAL', 'EARNINGS_RECEIVED') NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL,
    description VARCHAR(500) NOT NULL,
    reference_id VARCHAR(100),
    balance_before DECIMAL(10,2),
    balance_after DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_escrow_id (escrow_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_user_created (user_id, created_at),
    
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    FOREIGN KEY (escrow_id) REFERENCES escrows(id) ON DELETE SET NULL
);

-- ===================================
-- WALLETS TABLE
-- ===================================
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_earned DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_spent DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_refunded DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'SUSPENDED', 'FROZEN', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_transaction_at TIMESTAMP NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_balance (balance),
    INDEX idx_last_transaction (last_transaction_at)
);

-- ===================================
-- RAZORPAY WEBHOOKS TABLE
-- ===================================
CREATE TABLE IF NOT EXISTS razorpay_webhooks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    razorpay_payment_id VARCHAR(100),
    razorpay_order_id VARCHAR(100),
    razorpay_signature VARCHAR(500),
    payment_id BIGINT,
    status ENUM('RECEIVED', 'PROCESSING', 'PROCESSED', 'FAILED', 'IGNORED') NOT NULL DEFAULT 'RECEIVED',
    payload TEXT,
    signature_verified BOOLEAN NOT NULL DEFAULT FALSE,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processing_attempts INT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    next_retry_at TIMESTAMP NULL,
    
    INDEX idx_event_id (event_id),
    INDEX idx_event_type (event_type),
    INDEX idx_razorpay_payment_id (razorpay_payment_id),
    INDEX idx_razorpay_order_id (razorpay_order_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status),
    INDEX idx_processed (processed),
    INDEX idx_received_at (received_at),
    
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL
);

-- ===================================
-- SAMPLE DATA FOR TESTING (OPTIONAL)
-- ===================================

-- Insert sample wallet for testing
-- INSERT INTO wallets (user_id, balance, status) VALUES 
-- (1, 1000.00, 'ACTIVE'),
-- (2, 500.00, 'ACTIVE'),
-- (3, 0.00, 'ACTIVE');

-- ===================================
-- VERIFICATION QUERIES
-- ===================================

-- Check if all tables are created
SELECT TABLE_NAME, TABLE_ROWS 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'campusworks_payment';

-- Show table structures
-- DESCRIBE payments;
-- DESCRIBE escrows;
-- DESCRIBE transactions;
-- DESCRIBE wallets;
-- DESCRIBE razorpay_webhooks;

COMMIT;
