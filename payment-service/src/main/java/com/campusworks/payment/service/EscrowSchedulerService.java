package com.campusworks.payment.service;

import com.campusworks.payment.entity.Escrow;
import com.campusworks.payment.entity.EscrowStatus;
import com.campusworks.payment.repository.EscrowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowSchedulerService {
    
    private final EscrowRepository escrowRepository;
    private final PaymentService paymentService;
    
    @Value("${payment.escrow.auto-check-interval}")
    private Long autoCheckInterval;
    
    @Value("${payment.escrow.notification-enabled}")
    private Boolean notificationEnabled;
    
    /**
     * 🔥 CORE SCHEDULED TASK: Check for expired task deadlines
     * Runs every hour to check if any escrows have expired based on task deadline
     */
    @Scheduled(fixedRateString = "${payment.escrow.auto-check-interval}")
    public void checkExpiredTaskDeadlines() {
        log.info("⏰ Starting scheduled check for expired task deadlines...");
        
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            
            // Find all escrows that are FUNDED but expired based on task deadline
            List<Escrow> expiredEscrows = escrowRepository.findExpiredFundedEscrows(currentTime);
            
            if (expiredEscrows.isEmpty()) {
                log.info("✅ No expired escrows found");
                return;
            }
            
            log.info("🔍 Found {} expired escrows to process", expiredEscrows.size());
            
            int processedCount = 0;
            int errorCount = 0;
            
            for (Escrow escrow : expiredEscrows) {
                try {
                    log.info("⏰ Processing expired escrow: Task={}, Escrow={}, Expired at: {}", 
                            escrow.getTaskId(), escrow.getId(), escrow.getExpiresAt());
                    
                    // Process automatic refund
                    paymentService.processExpiredTaskDeadline(escrow.getTaskId());
                    processedCount++;
                    
                } catch (Exception e) {
                    log.error("❌ Error processing expired escrow {}: {}", escrow.getId(), e.getMessage(), e);
                    errorCount++;
                }
            }
            
            log.info("🎉 Expired escrow processing completed: Processed={}, Errors={}", processedCount, errorCount);
            
        } catch (Exception e) {
            log.error("❌ Error in scheduled escrow deadline check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send warning notifications for escrows expiring soon
     * Runs every 6 hours
     */
    @Scheduled(fixedRate = 21600000) // 6 hours
    public void sendExpirationWarnings() {
        if (!notificationEnabled) {
            return;
        }
        
        log.info("📢 Checking for escrows expiring soon...");
        
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime warningTime = currentTime.plusHours(24); // 24 hours warning
            
            List<Escrow> expiringSoon = escrowRepository.findEscrowsExpiringSoon(currentTime, warningTime);
            
            if (expiringSoon.isEmpty()) {
                log.info("✅ No escrows expiring in next 24 hours");
                return;
            }
            
            log.info("⚠️ Found {} escrows expiring in next 24 hours", expiringSoon.size());
            
            for (Escrow escrow : expiringSoon) {
                try {
                    // TODO: Send notification to both client and worker
                    // This will be implemented when Notification Service is ready
                    log.info("📢 Escrow {} for task {} expires at: {}", 
                            escrow.getId(), escrow.getTaskId(), escrow.getExpiresAt());
                    
                } catch (Exception e) {
                    log.error("❌ Error sending expiration warning for escrow {}: {}", escrow.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error in scheduled expiration warning check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup old processed transactions and webhooks
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldRecords() {
        log.info("🧹 Starting cleanup of old records...");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90); // Keep 90 days
            
            // TODO: Implement cleanup logic for old transactions and webhooks
            // This is a future enhancement for database maintenance
            
            log.info("✅ Cleanup completed");
            
        } catch (Exception e) {
            log.error("❌ Error in scheduled cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manual trigger for testing - check expired deadlines immediately
     */
    public void manuallyTriggerDeadlineCheck() {
        log.info("🔧 Manually triggering deadline check...");
        checkExpiredTaskDeadlines();
    }
    
    /**
     * Get configuration info
     */
    public String getSchedulerConfiguration() {
        return String.format("Auto-check interval: %d ms (%d hours), Notifications: %s", 
                autoCheckInterval, autoCheckInterval / 3600000, notificationEnabled);
    }
}
