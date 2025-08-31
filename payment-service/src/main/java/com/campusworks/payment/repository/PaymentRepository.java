package com.campusworks.payment.repository;

import com.campusworks.payment.entity.Payment;
import com.campusworks.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find by Razorpay identifiers
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    
    // Find by task and users
    Optional<Payment> findByTaskId(Long taskId);
    List<Payment> findByClientUserId(Long clientUserId);
    List<Payment> findByWorkerUserId(Long workerUserId);
    
    // Find by status
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusIn(List<PaymentStatus> statuses);
    
    // Find payments by user (both client and worker)
    @Query("SELECT p FROM Payment p WHERE p.clientUserId = :userId OR p.workerUserId = :userId")
    List<Payment> findByUserId(@Param("userId") Long userId);
    
    // Find payments by user and status
    @Query("SELECT p FROM Payment p WHERE (p.clientUserId = :userId OR p.workerUserId = :userId) AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);
    
    // Find failed payments that can be retried
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt > :since")
    List<Payment> findFailedPaymentsSince(@Param("since") LocalDateTime since);
    
    // Find pending payments older than specified time
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :before")
    List<Payment> findPendingPaymentsBefore(@Param("before") LocalDateTime before);
    
    // Statistics queries
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.completedAt BETWEEN :startDate AND :endDate")
    Long countCompletedPaymentsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.completedAt BETWEEN :startDate AND :endDate")
    Double sumCompletedPaymentsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // User statistics
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.clientUserId = :userId AND p.status = 'COMPLETED'")
    Long countCompletedPaymentsByClient(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.workerUserId = :userId AND p.status = 'COMPLETED'")
    Long countCompletedPaymentsByWorker(@Param("userId") Long userId);
    
    @Query("SELECT SUM(p.workerAmount) FROM Payment p WHERE p.workerUserId = :userId AND p.status = 'COMPLETED'")
    Double sumEarningsByWorker(@Param("userId") Long userId);
}
