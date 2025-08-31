package com.campusworks.payment.controller;

import com.campusworks.payment.entity.Escrow;
import com.campusworks.payment.entity.Payment;
import com.campusworks.payment.entity.Transaction;
import com.campusworks.payment.entity.Wallet;
import com.campusworks.payment.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 💰 CREATE PAYMENT: Client initiates payment for winning bid
     */
    @PostMapping("/tasks/{taskId}/create")
    public ResponseEntity<?> createTaskPayment(@PathVariable Long taskId, HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated", "message", "X-User-Id header missing"));
            }
            
            Long clientUserId = Long.valueOf(userIdHeader);
            log.info("💰 Creating payment for task: {}, client: {}", taskId, clientUserId);
            
            Payment payment = paymentService.createTaskPayment(taskId, clientUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment created successfully");
            response.put("payment", Map.of(
                    "id", payment.getId(),
                    "taskId", payment.getTaskId(),
                    "amount", payment.getAmount(),
                    "platformFee", payment.getPlatformFee(),
                    "workerAmount", payment.getWorkerAmount(),
                    "status", payment.getStatus(),
                    "razorpayOrderId", payment.getRazorpayOrderId(),
                    "currency", payment.getCurrency(),
                    "description", payment.getDescription()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (RazorpayException e) {
            log.error("❌ Razorpay error creating payment for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Payment creation failed", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error creating payment for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment creation failed", "message", e.getMessage()));
        }
    }
    
    /**
     * ✅ ACCEPT WORK: Client accepts work and releases payment to worker
     */
    @PostMapping("/tasks/{taskId}/accept")
    public ResponseEntity<?> acceptWorkAndReleasePayment(@PathVariable Long taskId, 
                                                       @RequestBody Map<String, String> requestBody,
                                                       HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated", "message", "X-User-Id header missing"));
            }
            
            Long clientUserId = Long.valueOf(userIdHeader);
            String acceptanceReason = requestBody.getOrDefault("reason", "Work accepted by client");
            
            log.info("✅ Client {} accepting work for task: {}", clientUserId, taskId);
            
            paymentService.acceptWorkAndReleasePayment(taskId, clientUserId, acceptanceReason);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Work accepted and payment released to worker",
                    "taskId", taskId,
                    "action", "PAYMENT_RELEASED"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error accepting work for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to accept work", "message", e.getMessage()));
        }
    }
    
    /**
     * ❌ REJECT WORK: Client rejects work and gets refund
     */
    @PostMapping("/tasks/{taskId}/reject")
    public ResponseEntity<?> rejectWorkAndRefundPayment(@PathVariable Long taskId, 
                                                      @RequestBody Map<String, String> requestBody,
                                                      HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated", "message", "X-User-Id header missing"));
            }
            
            Long clientUserId = Long.valueOf(userIdHeader);
            String rejectionReason = requestBody.getOrDefault("reason", "Work rejected by client");
            
            log.info("❌ Client {} rejecting work for task: {}", clientUserId, taskId);
            
            paymentService.rejectWorkAndRefundPayment(taskId, clientUserId, rejectionReason);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Work rejected and payment refunded to client",
                    "taskId", taskId,
                    "action", "PAYMENT_REFUNDED"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error rejecting work for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to reject work", "message", e.getMessage()));
        }
    }
    
    /**
     * 📊 GET PAYMENT DETAILS: Get payment info for a task
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskPaymentDetails(@PathVariable Long taskId, HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated", "message", "X-User-Id header missing"));
            }
            
            Payment payment = paymentService.getPaymentByTaskId(taskId);
            Escrow escrow = paymentService.getEscrowByTaskId(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payment", Map.of(
                    "id", payment.getId(),
                    "taskId", payment.getTaskId(),
                    "amount", payment.getAmount(),
                    "platformFee", payment.getPlatformFee(),
                    "workerAmount", payment.getWorkerAmount(),
                    "status", payment.getStatus(),
                    "paymentMethod", payment.getPaymentMethod(),
                    "createdAt", payment.getCreatedAt(),
                    "completedAt", payment.getCompletedAt()
            ));
            response.put("escrow", Map.of(
                    "id", escrow.getId(),
                    "status", escrow.getStatus(),
                    "expiresAt", escrow.getExpiresAt(),
                    "createdAt", escrow.getCreatedAt(),
                    "releasedAt", escrow.getReleasedAt(),
                    "refundedAt", escrow.getRefundedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting payment details for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Payment not found", "message", e.getMessage()));
        }
    }
    
    /**
     * 👛 GET USER WALLET: Get wallet details for authenticated user
     */
    @GetMapping("/wallet")
    public ResponseEntity<?> getUserWallet(HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated", "message", "X-User-Id header missing"));
            }
            
            Long userId = Long.valueOf(userIdHeader);
            Wallet wallet = paymentService.getUserWallet(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("wallet", Map.of(
                    "id", wallet.getId(),
                    "userId", wallet.getUserId(),
                    "balance", wallet.getBalance(),
                    "totalEarned", wallet.getTotalEarned(),
                    "totalSpent", wallet.getTotalSpent(),
                    "totalRefunded", wallet.getTotalRefunded(),
                    "status", wallet.getStatus(),
                    "lastTransactionAt", wallet.getLastTransactionAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting wallet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get wallet", "message", e.getMessage()));
        }
    }
    
    /**
     * 📋 GET USER TRANSACTIONS: Get transaction history for authenticated user
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getUserTransactions(HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated", "message", "X-User-Id header missing"));
            }
            
            Long userId = Long.valueOf(userIdHeader);
            List<Transaction> transactions = paymentService.getUserTransactions(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions.stream().map(t -> Map.of(
                    "id", t.getId(),
                    "amount", t.getAmount(),
                    "type", t.getType(),
                    "status", t.getStatus(),
                    "description", t.getDescription(),
                    "balanceBefore", t.getBalanceBefore(),
                    "balanceAfter", t.getBalanceAfter(),
                    "createdAt", t.getCreatedAt(),
                    "processedAt", t.getProcessedAt()
            )).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting transactions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get transactions", "message", e.getMessage()));
        }
    }
    
    /**
     * 📊 GET ESCROW DETAILS: Get escrow status for a task
     */
    @GetMapping("/escrows/task/{taskId}")
    public ResponseEntity<?> getTaskEscrowDetails(@PathVariable Long taskId, HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated", "message", "X-User-Id header missing"));
            }
            
            Escrow escrow = paymentService.getEscrowByTaskId(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            Map<String, Object> escrowData = new HashMap<>();
            escrowData.put("id", escrow.getId());
            escrowData.put("taskId", escrow.getTaskId());
            escrowData.put("totalAmount", escrow.getTotalAmount());
            escrowData.put("platformFee", escrow.getPlatformFee());
            escrowData.put("workerAmount", escrow.getWorkerAmount());
            escrowData.put("status", escrow.getStatus());
            escrowData.put("expiresAt", escrow.getExpiresAt());
            escrowData.put("isExpired", escrow.isExpired());
            escrowData.put("canBeReleased", escrow.canBeReleased());
            escrowData.put("canBeRefunded", escrow.canBeRefunded());
            escrowData.put("createdAt", escrow.getCreatedAt());
            escrowData.put("releasedAt", escrow.getReleasedAt());
            escrowData.put("refundedAt", escrow.getRefundedAt());
            
            response.put("escrow", escrowData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting escrow details for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Escrow not found", "message", e.getMessage()));
        }
    }
}
