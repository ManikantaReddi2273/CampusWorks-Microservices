package com.campusworks.payment.repository;

import com.campusworks.payment.entity.Escrow;
import com.campusworks.payment.entity.EscrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, Long> {
    
    // Find by task and users
    Optional<Escrow> findByTaskId(Long taskId);
    Optional<Escrow> findByPaymentId(Long paymentId);
    List<Escrow> findByClientUserId(Long clientUserId);
    List<Escrow> findByWorkerUserId(Long workerUserId);
    
    // Find by status
    List<Escrow> findByStatus(EscrowStatus status);
    List<Escrow> findByStatusIn(List<EscrowStatus> statuses);
    
    // 🔥 KEY QUERY: Find escrows expired based on task deadline
    @Query("SELECT e FROM Escrow e WHERE e.status = 'FUNDED' AND e.expiresAt < :currentTime")
    List<Escrow> findExpiredFundedEscrows(@Param("currentTime") LocalDateTime currentTime);
    
    // Find escrows expiring soon (for notifications)
    @Query("SELECT e FROM Escrow e WHERE e.status = 'FUNDED' AND e.expiresAt BETWEEN :currentTime AND :warningTime")
    List<Escrow> findEscrowsExpiringSoon(@Param("currentTime") LocalDateTime currentTime, 
                                        @Param("warningTime") LocalDateTime warningTime);
    
    // Find all expired escrows (regardless of status)
    @Query("SELECT e FROM Escrow e WHERE e.expiresAt < :currentTime")
    List<Escrow> findAllExpiredEscrows(@Param("currentTime") LocalDateTime currentTime);
    
    // Find escrows by user (both client and worker)
    @Query("SELECT e FROM Escrow e WHERE e.clientUserId = :userId OR e.workerUserId = :userId")
    List<Escrow> findByUserId(@Param("userId") Long userId);
    
    // Find escrows by user and status
    @Query("SELECT e FROM Escrow e WHERE (e.clientUserId = :userId OR e.workerUserId = :userId) AND e.status = :status")
    List<Escrow> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") EscrowStatus status);
    
    // Find escrows that can be released (FUNDED status)
    @Query("SELECT e FROM Escrow e WHERE e.status = 'FUNDED'")
    List<Escrow> findReleasableEscrows();
    
    // Find escrows that can be refunded
    @Query("SELECT e FROM Escrow e WHERE e.status IN ('FUNDED', 'CREATED')")
    List<Escrow> findRefundableEscrows();
    
    // Statistics queries
    @Query("SELECT COUNT(e) FROM Escrow e WHERE e.status = 'RELEASED' AND e.releasedAt BETWEEN :startDate AND :endDate")
    Long countReleasedEscrowsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(e.workerAmount) FROM Escrow e WHERE e.status = 'RELEASED' AND e.releasedAt BETWEEN :startDate AND :endDate")
    Double sumReleasedAmountsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(e) FROM Escrow e WHERE e.status = 'REFUNDED' AND e.refundedAt BETWEEN :startDate AND :endDate")
    Long countRefundedEscrowsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Active escrows (CREATED or FUNDED)
    @Query("SELECT COUNT(e) FROM Escrow e WHERE e.status IN ('CREATED', 'FUNDED')")
    Long countActiveEscrows();
    
    @Query("SELECT SUM(e.totalAmount) FROM Escrow e WHERE e.status IN ('CREATED', 'FUNDED')")
    Double sumActiveEscrowAmounts();
}
