package com.campusworks.payment.controller;

import com.campusworks.payment.service.EscrowSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final EscrowSchedulerService escrowSchedulerService;
    
    /**
     * 🔧 MANUAL TRIGGER: Manually trigger deadline check for testing
     */
    @PostMapping("/trigger-deadline-check")
    public ResponseEntity<?> manuallyTriggerDeadlineCheck() {
        try {
            log.info("🔧 Admin manually triggering deadline check");
            
            escrowSchedulerService.manuallyTriggerDeadlineCheck();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Deadline check triggered successfully",
                    "action", "MANUAL_DEADLINE_CHECK"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error in manual deadline check: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to trigger deadline check", "message", e.getMessage()));
        }
    }
    
    /**
     * ⚙️ GET CONFIGURATION: Get scheduler configuration
     */
    @GetMapping("/config")
    public ResponseEntity<?> getSchedulerConfiguration() {
        try {
            String config = escrowSchedulerService.getSchedulerConfiguration();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "configuration", config
            ));
            
        } catch (Exception e) {
            log.error("❌ Error getting configuration: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get configuration", "message", e.getMessage()));
        }
    }
}
