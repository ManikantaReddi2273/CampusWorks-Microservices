package com.campusworks.payment.service;

import com.campusworks.payment.entity.Payment;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {
    
    private final RazorpayClient razorpayClient;
    
    @Value("${payment.razorpay.currency}")
    private String currency;
    
    @Value("${payment.razorpay.company-name}")
    private String companyName;
    
    @Value("${payment.razorpay.webhook-secret}")
    private String webhookSecret;
    
    /**
     * Create Razorpay order for payment
     */
    public Order createOrder(Payment payment) throws RazorpayException {
        try {
            log.info("💰 Creating Razorpay order for payment ID: {}, amount: ₹{}", payment.getId(), payment.getAmount());
            
            // Convert amount to paise (Razorpay requires amount in smallest currency unit)
            BigDecimal amountInPaise = payment.getAmount().multiply(BigDecimal.valueOf(100));
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise.intValue());
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "payment_" + payment.getId() + "_" + System.currentTimeMillis());
            
            // Add notes for tracking
            JSONObject notes = new JSONObject();
            notes.put("payment_id", payment.getId().toString());
            notes.put("task_id", payment.getTaskId().toString());
            notes.put("client_user_id", payment.getClientUserId().toString());
            notes.put("worker_user_id", payment.getWorkerUserId().toString());
            notes.put("company", companyName);
            orderRequest.put("notes", notes);
            
            Order order = razorpayClient.orders.create(orderRequest);
            
            log.info("✅ Razorpay order created successfully: " + order.get("id"));
            log.info("📄 Order details: Amount=₹" + payment.getAmount() + ", Currency=" + currency + ", Receipt=" + order.get("receipt"));
            
            return order;
            
        } catch (RazorpayException e) {
            log.error("❌ Failed to create Razorpay order for payment {}: {}", payment.getId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Verify payment signature from Razorpay
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            log.info("🔐 Verifying payment signature for order: {}, payment: {}", orderId, paymentId);
            
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            
            boolean isValid = Utils.verifyPaymentSignature(options, webhookSecret);
            
            if (isValid) {
                log.info("✅ Payment signature verified successfully for payment: {}", paymentId);
            } else {
                log.warn("⚠️ Payment signature verification failed for payment: {}", paymentId);
            }
            
            return isValid;
            
        } catch (RazorpayException e) {
            log.error("❌ Error verifying payment signature: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            log.debug("🔐 Verifying webhook signature");
            
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            
            if (isValid) {
                log.info("✅ Webhook signature verified successfully");
            } else {
                log.warn("⚠️ Webhook signature verification failed");
            }
            
            return isValid;
            
        } catch (RazorpayException e) {
            log.error("❌ Error verifying webhook signature: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Create refund for a payment
     */
    public com.razorpay.Refund createRefund(String paymentId, BigDecimal amount, String reason) throws RazorpayException {
        try {
            log.info("💸 Creating refund for payment: " + paymentId + ", amount: ₹" + amount + ", reason: " + reason);
            
            // Convert amount to paise
            BigDecimal amountInPaise = amount.multiply(BigDecimal.valueOf(100));
            
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amountInPaise.intValue());
            refundRequest.put("speed", "normal"); // normal or optimum
            
            // Add notes
            JSONObject notes = new JSONObject();
            notes.put("reason", reason);
            notes.put("company", companyName);
            refundRequest.put("notes", notes);
            
            com.razorpay.Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
            
            log.info("✅ Refund created successfully: " + refund.get("id"));
            
            return refund;
            
        } catch (RazorpayException e) {
            log.error("❌ Failed to create refund for payment {}: {}", paymentId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get payment details from Razorpay
     */
    public com.razorpay.Payment getPaymentDetails(String paymentId) throws RazorpayException {
        try {
            log.info("🔍 Fetching payment details for: {}", paymentId);
            
            com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);
            
            log.info("✅ Payment details fetched: Status={}, Amount=₹{}", 
                    payment.get("status"), 
                    new BigDecimal(payment.get("amount").toString()).divide(BigDecimal.valueOf(100)));
            
            return payment;
            
        } catch (RazorpayException e) {
            log.error("❌ Failed to fetch payment details for {}: {}", paymentId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get order details from Razorpay
     */
    public Order getOrderDetails(String orderId) throws RazorpayException {
        try {
            log.info("🔍 Fetching order details for: {}", orderId);
            
            Order order = razorpayClient.orders.fetch(orderId);
            
            log.info("✅ Order details fetched: Status={}, Amount=₹{}", 
                    order.get("status"), 
                    new BigDecimal(order.get("amount").toString()).divide(BigDecimal.valueOf(100)));
            
            return order;
            
        } catch (RazorpayException e) {
            log.error("❌ Failed to fetch order details for {}: {}", orderId, e.getMessage());
            throw e;
        }
    }
}
