package com.campusworks.payment.entity;

public enum EscrowStatus {
    CREATED,        // Escrow created, waiting for payment
    FUNDED,         // Payment received, money held in escrow
    RELEASED,       // Money released to worker
    REFUNDED,       // Money refunded to client
    DISPUTED        // Under dispute resolution
}
