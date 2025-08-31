package com.campusworks.payment.repository;

import com.campusworks.payment.entity.Wallet;
import com.campusworks.payment.entity.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    // Find by user
    Optional<Wallet> findByUserId(Long userId);
    
    // Find by status
    List<Wallet> findByStatus(WalletStatus status);
    
    // Find wallets with sufficient balance
    @Query("SELECT w FROM Wallet w WHERE w.balance >= :minBalance AND w.status = 'ACTIVE'")
    List<Wallet> findWalletsWithMinBalance(@Param("minBalance") BigDecimal minBalance);
    
    // Find wallets with zero balance
    @Query("SELECT w FROM Wallet w WHERE w.balance = 0")
    List<Wallet> findEmptyWallets();
    
    // Find wallets that haven't been used recently
    @Query("SELECT w FROM Wallet w WHERE w.lastTransactionAt < :before OR w.lastTransactionAt IS NULL")
    List<Wallet> findInactiveWalletsSince(@Param("before") LocalDateTime before);
    
    // Statistics queries
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.status = 'ACTIVE'")
    Long countActiveWallets();
    
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.status = 'ACTIVE'")
    BigDecimal sumActiveWalletBalances();
    
    @Query("SELECT SUM(w.totalEarned) FROM Wallet w")
    BigDecimal sumTotalEarnings();
    
    @Query("SELECT SUM(w.totalSpent) FROM Wallet w")
    BigDecimal sumTotalSpending();
    
    // Top earners
    @Query("SELECT w FROM Wallet w WHERE w.status = 'ACTIVE' ORDER BY w.totalEarned DESC LIMIT :limit")
    List<Wallet> findTopEarners(@Param("limit") int limit);
    
    // Recent activity
    @Query("SELECT w FROM Wallet w WHERE w.lastTransactionAt IS NOT NULL ORDER BY w.lastTransactionAt DESC LIMIT :limit")
    List<Wallet> findRecentlyActiveWallets(@Param("limit") int limit);
    
    // Check if user has wallet
    boolean existsByUserId(Long userId);
    
    // Find users with high balances (for security monitoring)
    @Query("SELECT w FROM Wallet w WHERE w.balance > :threshold AND w.status = 'ACTIVE'")
    List<Wallet> findHighBalanceWallets(@Param("threshold") BigDecimal threshold);
}
