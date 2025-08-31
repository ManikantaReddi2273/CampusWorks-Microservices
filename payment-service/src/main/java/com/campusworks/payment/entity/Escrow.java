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
@Table(name = "escrows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Escrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false, unique = true)
    private Long taskId;
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    @Column(name = "client_user_id", nullable = false)
    private Long clientUserId;
    
    @Column(name = "worker_user_id", nullable = false)
    private Long workerUserId;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;
    
    @Column(name = "worker_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal workerAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EscrowStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "released_at")
    private LocalDateTime releasedAt;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "release_reason", length = 500)
    private String releaseReason;
    
    @Column(name = "refund_reason", length = 500)
    private String refundReason;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean canBeReleased() {
        return status == EscrowStatus.FUNDED;
    }
    
    public boolean canBeRefunded() {
        return status == EscrowStatus.FUNDED || status == EscrowStatus.CREATED;
    }
}
