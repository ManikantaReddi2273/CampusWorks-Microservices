package com.campusworks.payment.repository;

import com.campusworks.payment.entity.Transaction;
import com.campusworks.payment.entity.TransactionStatus;
import com.campusworks.payment.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find by user
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, TransactionStatus status);
    
    // Find by task, payment, escrow
    List<Transaction> findByTaskId(Long taskId);
    List<Transaction> findByPaymentId(Long paymentId);
    List<Transaction> findByEscrowId(Long escrowId);
    
    // Find by type and status
    List<Transaction> findByType(TransactionType type);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByTypeAndStatus(TransactionType type, TransactionStatus status);
    
    // Find by reference ID (external identifiers)
    List<Transaction> findByReferenceId(String referenceId);
    
    // Find transactions in date range
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find failed transactions that can be retried
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' AND t.createdAt > :since")
    List<Transaction> findFailedTransactionsSince(@Param("since") LocalDateTime since);
    
    // User transaction statistics
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.status = 'COMPLETED'")
    Double sumCompletedTransactionsByUserAndType(@Param("userId") Long userId, @Param("type") TransactionType type);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.status = 'COMPLETED'")
    Long countCompletedTransactionsByUserAndType(@Param("userId") Long userId, @Param("type") TransactionType type);
    
    // Platform statistics
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'PLATFORM_FEE' AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Double sumPlatformFeesBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countCompletedTransactionsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Recent transactions for user
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.createdAt DESC LIMIT :limit")
    List<Transaction> findRecentTransactionsByUser(@Param("userId") Long userId, @Param("limit") int limit);
    
    // Pending transactions older than specified time
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :before")
    List<Transaction> findPendingTransactionsBefore(@Param("before") LocalDateTime before);
}
