package com.campusworks.payment.entity;

public enum TransactionType {
    PAYMENT_RECEIVED,       // Client pays for task
    ESCROW_FUNDED,         // Money moved to escrow
    PAYMENT_RELEASED,      // Money released to worker
    PAYMENT_REFUNDED,      // Money refunded to client
    PLATFORM_FEE,          // Platform fee deducted
    WALLET_DEPOSIT,        // Money added to wallet
    WALLET_WITHDRAWAL,     // Money withdrawn from wallet
    EARNINGS_RECEIVED      // Worker receives earnings
}
