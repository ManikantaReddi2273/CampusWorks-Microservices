package com.campusworks.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "razorpay_webhooks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayWebhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;
    
    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;
    
    @Column(name = "razorpay_signature", length = 500)
    private String razorpaySignature;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WebhookStatus status = WebhookStatus.RECEIVED;
    
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "signature_verified", nullable = false)
    @Builder.Default
    private Boolean signatureVerified = false;
    
    @Column(name = "processed", nullable = false)
    @Builder.Default
    private Boolean processed = false;
    
    @Column(name = "processing_attempts", nullable = false)
    @Builder.Default
    private Integer processingAttempts = 0;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    public void markAsProcessed() {
        this.processed = true;
        this.status = WebhookStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.processed = false;
        this.status = WebhookStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processingAttempts++;
        
        int delayMinutes = (int) Math.pow(2, Math.min(processingAttempts, 6));
        this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
    }
    
    public boolean shouldRetry() {
        return !processed && 
               processingAttempts < 5 && 
               (nextRetryAt == null || LocalDateTime.now().isAfter(nextRetryAt));
    }
}
