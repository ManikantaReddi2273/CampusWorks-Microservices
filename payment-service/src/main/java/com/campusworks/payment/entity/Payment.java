package com.campusworks.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @Column(name = "client_user_id", nullable = false)
    private Long clientUserId;
    
    @Column(name = "worker_user_id", nullable = false)
    private Long workerUserId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;
    
    @Column(name = "worker_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal workerAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;
    
    @Column(name = "razorpay_payment_id", unique = true)
    private String razorpayPaymentId;
    
    @Column(name = "razorpay_signature")
    private String razorpaySignature;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @PrePersist
    public void prePersist() {
        if (currency == null) {
            currency = "INR";
        }
    }
}
