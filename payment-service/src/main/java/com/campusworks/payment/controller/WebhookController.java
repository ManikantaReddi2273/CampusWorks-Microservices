package com.campusworks.payment.controller;

import com.campusworks.payment.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    
    private final WebhookService webhookService;
    
    /**
     * 🔔 RAZORPAY WEBHOOK: Handle payment notifications from Razorpay
     */
    @PostMapping("/razorpay")
    public ResponseEntity<?> handleRazorpayWebhook(@RequestBody String payload,
                                                 @RequestHeader("X-Razorpay-Signature") String signature,
                                                 HttpServletRequest request) {
        try {
            log.info("🔔 Received Razorpay webhook");
            log.debug("📄 Webhook payload length: {} characters", payload.length());
            log.debug("🔐 Signature: {}", signature.substring(0, Math.min(signature.length(), 20)) + "...");
            
            // Process webhook asynchronously
            webhookService.processRazorpayWebhook(payload, signature);
            
            // Return 200 OK immediately to acknowledge receipt
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Webhook received and queued for processing"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error handling Razorpay webhook: {}", e.getMessage(), e);
            
            // Still return 200 to prevent Razorpay from retrying
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Webhook received but processing failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 📊 WEBHOOK STATUS: Get webhook processing status (for debugging)
     */
    @GetMapping("/status")
    public ResponseEntity<?> getWebhookStatus() {
        try {
            Map<String, Object> status = webhookService.getWebhookProcessingStatus();
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("❌ Error getting webhook status: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get webhook status", "message", e.getMessage()));
        }
    }
}
