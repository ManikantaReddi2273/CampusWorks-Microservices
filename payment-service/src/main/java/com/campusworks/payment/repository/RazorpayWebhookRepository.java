package com.campusworks.payment.repository;

import com.campusworks.payment.entity.RazorpayWebhook;
import com.campusworks.payment.entity.WebhookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RazorpayWebhookRepository extends JpaRepository<RazorpayWebhook, Long> {
    
    // Find by identifiers
    Optional<RazorpayWebhook> findByEventId(String eventId);
    List<RazorpayWebhook> findByRazorpayPaymentId(String razorpayPaymentId);
    List<RazorpayWebhook> findByRazorpayOrderId(String razorpayOrderId);
    Optional<RazorpayWebhook> findByPaymentId(Long paymentId);
    
    // Find by event type
    List<RazorpayWebhook> findByEventType(String eventType);
    List<RazorpayWebhook> findByEventTypeAndStatus(String eventType, WebhookStatus status);
    
    // Find by status
    List<RazorpayWebhook> findByStatus(WebhookStatus status);
    List<RazorpayWebhook> findByProcessed(Boolean processed);
    
    // Find webhooks that need processing
    @Query("SELECT w FROM RazorpayWebhook w WHERE w.processed = false AND w.status != 'IGNORED' ORDER BY w.receivedAt ASC")
    List<RazorpayWebhook> findUnprocessedWebhooks();
    
    // Find webhooks that need retry
    @Query("SELECT w FROM RazorpayWebhook w WHERE w.processed = false AND w.status = 'FAILED' AND w.processingAttempts < 5 AND (w.nextRetryAt IS NULL OR w.nextRetryAt <= :currentTime)")
    List<RazorpayWebhook> findWebhooksForRetry(@Param("currentTime") LocalDateTime currentTime);
    
    // Find failed webhooks
    @Query("SELECT w FROM RazorpayWebhook w WHERE w.status = 'FAILED' AND w.processingAttempts >= 5")
    List<RazorpayWebhook> findPermanentlyFailedWebhooks();
    
    // Find recent webhooks
    List<RazorpayWebhook> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find webhooks by signature verification
    List<RazorpayWebhook> findBySignatureVerified(Boolean signatureVerified);
    
    // Statistics queries
    @Query("SELECT COUNT(w) FROM RazorpayWebhook w WHERE w.status = 'PROCESSED' AND w.receivedAt BETWEEN :startDate AND :endDate")
    Long countProcessedWebhooksBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(w) FROM RazorpayWebhook w WHERE w.status = 'FAILED' AND w.receivedAt BETWEEN :startDate AND :endDate")
    Long countFailedWebhooksBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Check for duplicate events
    boolean existsByEventId(String eventId);
    
    // Find old processed webhooks for cleanup
    @Query("SELECT w FROM RazorpayWebhook w WHERE w.processed = true AND w.receivedAt < :before")
    List<RazorpayWebhook> findOldProcessedWebhooks(@Param("before") LocalDateTime before);
}
