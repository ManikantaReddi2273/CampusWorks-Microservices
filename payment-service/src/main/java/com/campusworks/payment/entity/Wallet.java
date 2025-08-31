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
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "total_earned", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalEarned = BigDecimal.ZERO;
    
    @Column(name = "total_spent", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Column(name = "total_refunded", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunded = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;
    
    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
            this.lastTransactionAt = LocalDateTime.now();
        }
    }
    
    public void deductBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 && 
            this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
            this.lastTransactionAt = LocalDateTime.now();
        }
    }
    
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    public boolean isActive() {
        return this.status == WalletStatus.ACTIVE;
    }
    
    public void addEarnings(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalEarned = this.totalEarned.add(amount);
            addBalance(amount);
        }
    }
    
    public void addSpending(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalSpent = this.totalSpent.add(amount);
        }
    }
    
    public void addRefund(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalRefunded = this.totalRefunded.add(amount);
            addBalance(amount);
        }
    }
}
