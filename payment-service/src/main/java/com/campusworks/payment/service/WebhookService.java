package com.campusworks.payment.service;

import com.campusworks.payment.entity.RazorpayWebhook;
import com.campusworks.payment.entity.WebhookStatus;
import com.campusworks.payment.repository.RazorpayWebhookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final RazorpayWebhookRepository webhookRepository;
    private final RazorpayService razorpayService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    
    /**
     * Process Razorpay webhook asynchronously
     */
    @Async
    @Transactional
    public void processRazorpayWebhook(String payload, String signature) {
        log.info("🔔 Processing Razorpay webhook asynchronously");
        
        try {
            // 1. Parse webhook payload
            JsonNode webhookData = objectMapper.readTree(payload);
            String eventType = webhookData.get("event").asText();
            String eventId = webhookData.get("event").asText() + "_" + System.currentTimeMillis();
            
            log.info("📋 Webhook event type: {}", eventType);
            
            // 2. Check for duplicate webhook
            if (webhookRepository.existsByEventId(eventId)) {
                log.info("⚠️ Duplicate webhook ignored: {}", eventId);
                return;
            }
            
            // 3. Verify webhook signature
            boolean signatureValid = razorpayService.verifyWebhookSignature(payload, signature);
            
            // 4. Create webhook record
            RazorpayWebhook webhook = RazorpayWebhook.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .payload(payload)
                    .signatureVerified(signatureValid)
                    .status(WebhookStatus.RECEIVED)
                    .build();
            
            // Extract payment/order IDs if available
            JsonNode paymentData = webhookData.get("payload");
            if (paymentData != null) {
                JsonNode payment = paymentData.get("payment");
                JsonNode order = paymentData.get("order");
                
                if (payment != null && payment.get("entity") != null) {
                    webhook.setRazorpayPaymentId(payment.get("entity").get("id").asText());
                    webhook.setRazorpayOrderId(payment.get("entity").get("order_id").asText());
                }
                
                if (order != null && order.get("entity") != null) {
                    webhook.setRazorpayOrderId(order.get("entity").get("id").asText());
                }
            }
            
            webhook = webhookRepository.save(webhook);
            
            if (!signatureValid) {
                webhook.markAsFailed("Invalid webhook signature");
                webhookRepository.save(webhook);
                log.error("❌ Webhook signature verification failed: {}", eventId);
                return;
            }
            
            // 5. Process webhook based on event type
            webhook.setStatus(WebhookStatus.PROCESSING);
            webhookRepository.save(webhook);
            
            switch (eventType) {
                case "payment.captured":
                    processPaymentCaptured(webhook, paymentData);
                    break;
                case "payment.failed":
                    processPaymentFailed(webhook, paymentData);
                    break;
                case "order.paid":
                    processOrderPaid(webhook, paymentData);
                    break;
                case "refund.created":
                    processRefundCreated(webhook, paymentData);
                    break;
                default:
                    log.info("ℹ️ Ignoring webhook event type: {}", eventType);
                    webhook.setStatus(WebhookStatus.IGNORED);
                    webhook.markAsProcessed();
                    webhookRepository.save(webhook);
                    break;
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing webhook: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process payment.captured event
     */
    private void processPaymentCaptured(RazorpayWebhook webhook, JsonNode paymentData) {
        try {
            JsonNode payment = paymentData.get("payment").get("entity");
            String paymentId = payment.get("id").asText();
            String orderId = payment.get("order_id").asText();
            
            log.info("💰 Processing payment captured: {}", paymentId);
            
            // Process successful payment
            paymentService.processSuccessfulPayment(paymentId, orderId, webhook.getRazorpaySignature());
            
            webhook.markAsProcessed();
            webhookRepository.save(webhook);
            
            log.info("✅ Payment captured processed successfully: {}", paymentId);
            
        } catch (Exception e) {
            log.error("❌ Error processing payment captured: {}", e.getMessage());
            webhook.markAsFailed("Error processing payment captured: " + e.getMessage());
            webhookRepository.save(webhook);
        }
    }
    
    /**
     * Process payment.failed event
     */
    private void processPaymentFailed(RazorpayWebhook webhook, JsonNode paymentData) {
        try {
            JsonNode payment = paymentData.get("payment").get("entity");
            String paymentId = payment.get("id").asText();
            String orderId = payment.get("order_id").asText();
            String errorDescription = payment.get("error_description").asText();
            
            log.info("❌ Processing payment failed: {}, Reason: {}", paymentId, errorDescription);
            
            // TODO: Mark payment as failed and handle cleanup
            // This will be implemented based on your business requirements
            
            webhook.markAsProcessed();
            webhookRepository.save(webhook);
            
            log.info("✅ Payment failed processed: {}", paymentId);
            
        } catch (Exception e) {
            log.error("❌ Error processing payment failed: {}", e.getMessage());
            webhook.markAsFailed("Error processing payment failed: " + e.getMessage());
            webhookRepository.save(webhook);
        }
    }
    
    /**
     * Process order.paid event
     */
    private void processOrderPaid(RazorpayWebhook webhook, JsonNode paymentData) {
        try {
            JsonNode order = paymentData.get("order").get("entity");
            String orderId = order.get("id").asText();
            
            log.info("📋 Processing order paid: {}", orderId);
            
            // This event is usually followed by payment.captured
            // We can use this for additional validation or logging
            
            webhook.markAsProcessed();
            webhookRepository.save(webhook);
            
            log.info("✅ Order paid processed: {}", orderId);
            
        } catch (Exception e) {
            log.error("❌ Error processing order paid: {}", e.getMessage());
            webhook.markAsFailed("Error processing order paid: " + e.getMessage());
            webhookRepository.save(webhook);
        }
    }
    
    /**
     * Process refund.created event
     */
    private void processRefundCreated(RazorpayWebhook webhook, JsonNode paymentData) {
        try {
            JsonNode refund = paymentData.get("refund").get("entity");
            String refundId = refund.get("id").asText();
            String paymentId = refund.get("payment_id").asText();
            
            log.info("💸 Processing refund created: {}, Payment: {}", refundId, paymentId);
            
            // TODO: Update payment and escrow status for refund
            // This will be implemented based on your business requirements
            
            webhook.markAsProcessed();
            webhookRepository.save(webhook);
            
            log.info("✅ Refund created processed: {}", refundId);
            
        } catch (Exception e) {
            log.error("❌ Error processing refund created: {}", e.getMessage());
            webhook.markAsFailed("Error processing refund created: " + e.getMessage());
            webhookRepository.save(webhook);
        }
    }
    
    /**
     * Get webhook processing status for monitoring
     */
    public Map<String, Object> getWebhookProcessingStatus() {
        Map<String, Object> status = new HashMap<>();
        
        long totalWebhooks = webhookRepository.count();
        long processedWebhooks = webhookRepository.findByProcessed(true).size();
        long failedWebhooks = webhookRepository.findByStatus(WebhookStatus.FAILED).size();
        long pendingWebhooks = webhookRepository.findUnprocessedWebhooks().size();
        
        status.put("total", totalWebhooks);
        status.put("processed", processedWebhooks);
        status.put("failed", failedWebhooks);
        status.put("pending", pendingWebhooks);
        status.put("success_rate", totalWebhooks > 0 ? (double) processedWebhooks / totalWebhooks * 100 : 0);
        status.put("last_check", LocalDateTime.now());
        
        return status;
    }
}
