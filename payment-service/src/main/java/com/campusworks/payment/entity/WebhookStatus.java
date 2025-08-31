package com.campusworks.payment.entity;

public enum WebhookStatus {
    RECEIVED,       // Webhook received
    PROCESSING,     // Being processed
    PROCESSED,      // Successfully processed
    FAILED,         // Processing failed
    IGNORED         // Ignored (duplicate or irrelevant)
}
