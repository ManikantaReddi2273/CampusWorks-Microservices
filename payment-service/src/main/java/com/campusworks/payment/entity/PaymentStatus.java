package com.campusworks.payment.entity;

public enum PaymentStatus {
    CREATED,        // Payment order created
    PENDING,        // Waiting for user payment
    PROCESSING,     // Payment being processed
    COMPLETED,      // Payment successful
    FAILED,         // Payment failed
    REFUNDED        // Payment refunded
}
