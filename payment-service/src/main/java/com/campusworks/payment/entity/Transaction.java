package com.campusworks.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "task_id")
    private Long taskId;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "escrow_id")
    private Long escrowId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "reference_id", length = 100)
    private String referenceId;
    
    @Column(name = "balance_before", precision = 10, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
