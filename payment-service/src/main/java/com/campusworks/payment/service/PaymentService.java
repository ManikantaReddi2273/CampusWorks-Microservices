package com.campusworks.payment.service;

import com.campusworks.payment.client.BiddingServiceClient;
import com.campusworks.payment.client.ProfileServiceClient;
import com.campusworks.payment.client.TaskServiceClient;
import com.campusworks.payment.dto.AddEarningsRequest;
import com.campusworks.payment.dto.BidResponse;
import com.campusworks.payment.dto.TaskResponse;
import com.campusworks.payment.dto.TaskStatusUpdateRequest;
import com.campusworks.payment.entity.*;
import com.campusworks.payment.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final EscrowRepository escrowRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final RazorpayService razorpayService;
    private final TaskServiceClient taskServiceClient;
    private final ProfileServiceClient profileServiceClient;
    private final BiddingServiceClient biddingServiceClient;
    
    @Value("${payment.platform-fee-percentage}")
    private BigDecimal platformFeePercentage;
    
    @Value("${payment.min-transaction-amount}")
    private BigDecimal minTransactionAmount;
    
    @Value("${payment.max-transaction-amount}")
    private BigDecimal maxTransactionAmount;
    
    /**
     * 🔥 CORE BUSINESS LOGIC: Create payment and escrow for winning bid
     * Escrow expiration is tied to task deadline (your improved logic)
     */
    @Transactional
    public Payment createTaskPayment(Long taskId, Long clientUserId) throws RazorpayException {
        log.info("💰 Creating payment for task: {}, client: {}", taskId, clientUserId);
        
        // 1. Get task details to get the deadline
        TaskResponse task = taskServiceClient.getTask(taskId);
        if (!task.isSuccess()) {
            throw new RuntimeException("Failed to fetch task details: " + task.getMessage());
        }
        
        // 2. Get winning bid details
        BidResponse winningBid = biddingServiceClient.getWinningBid(taskId);
        if (!winningBid.isSuccess()) {
            throw new RuntimeException("No winning bid found for task: " + taskId);
        }
        
        // 3. Validate amounts
        BigDecimal bidAmount = winningBid.getAmount();
        validateTransactionAmount(bidAmount);
        
        // 4. Calculate platform fee and worker amount
        BigDecimal platformFee = bidAmount.multiply(platformFeePercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal workerAmount = bidAmount.subtract(platformFee);
        
        log.info("💵 Payment breakdown: Total=₹{}, Platform Fee=₹{}, Worker Amount=₹{}", 
                bidAmount, platformFee, workerAmount);
        
        // 5. Create payment record
        Payment payment = Payment.builder()
                .taskId(taskId)
                .clientUserId(clientUserId)
                .workerUserId(winningBid.getBidderId())
                .amount(bidAmount)
                .platformFee(platformFee)
                .workerAmount(workerAmount)
                .status(PaymentStatus.CREATED)
                .paymentMethod(PaymentMethod.RAZORPAY_CARD) // Default, will be updated
                .currency("INR")
                .description("Payment for task: " + task.getTitle())
                .build();
        
        payment = paymentRepository.save(payment);
        log.info("✅ Payment record created with ID: {}", payment.getId());
        
        // 6. Create Razorpay order
        Order razorpayOrder = razorpayService.createOrder(payment);
        
        // 7. Update payment with Razorpay order ID
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);
        
        // 8. Create escrow with task deadline expiration (🔥 YOUR IMPROVED LOGIC)
        Escrow escrow = Escrow.builder()
                .taskId(taskId)
                .paymentId(payment.getId())
                .clientUserId(clientUserId)
                .workerUserId(winningBid.getBidderId())
                .totalAmount(bidAmount)
                .platformFee(platformFee)
                .workerAmount(workerAmount)
                .status(EscrowStatus.CREATED)
                .expiresAt(task.getTaskDeadline()) // 🔥 KEY: Use task deadline, not fixed 30 days
                .build();
        
        escrow = escrowRepository.save(escrow);
        log.info("🏦 Escrow created with ID: {}, expires at: {}", escrow.getId(), escrow.getExpiresAt());
        
        // 9. Create transaction record
        createTransaction(clientUserId, payment.getId(), null, bidAmount, 
                         TransactionType.PAYMENT_RECEIVED, TransactionStatus.PENDING,
                         "Payment created for task: " + task.getTitle(), 
                         razorpayOrder.get("id"));
        
        log.info("🎉 Payment creation completed successfully for task: {}", taskId);
        return payment;
    }
    
    /**
     * Process successful payment from Razorpay webhook
     */
    @Transactional
    public void processSuccessfulPayment(String razorpayPaymentId, String razorpayOrderId, String signature) {
        log.info("✅ Processing successful payment: {}", razorpayPaymentId);
        
        // 1. Find payment by order ID
        Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        if (paymentOpt.isEmpty()) {
            log.error("❌ Payment not found for Razorpay order: {}", razorpayOrderId);
            return;
        }
        
        Payment payment = paymentOpt.get();
        
        // 2. Verify signature
        boolean signatureValid = razorpayService.verifyPaymentSignature(razorpayOrderId, razorpayPaymentId, signature);
        if (!signatureValid) {
            log.error("❌ Invalid signature for payment: {}", razorpayPaymentId);
            markPaymentAsFailed(payment, "Invalid payment signature");
            return;
        }
        
        // 3. Update payment status
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(signature);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        
        // 4. Fund the escrow
        Optional<Escrow> escrowOpt = escrowRepository.findByPaymentId(payment.getId());
        if (escrowOpt.isPresent()) {
            Escrow escrow = escrowOpt.get();
            escrow.setStatus(EscrowStatus.FUNDED);
            escrowRepository.save(escrow);
            log.info("🏦 Escrow funded: {}", escrow.getId());
            
            // Create escrow funding transaction
            createTransaction(payment.getClientUserId(), payment.getId(), escrow.getId(), 
                             payment.getAmount(), TransactionType.ESCROW_FUNDED, TransactionStatus.COMPLETED,
                             "Escrow funded for task payment", razorpayPaymentId);
        }
        
        // 5. Update client wallet spending
        updateWalletSpending(payment.getClientUserId(), payment.getAmount());
        
        // 6. Notify bidding service
        biddingServiceClient.notifyPaymentConfirmed(getWinningBidId(payment.getTaskId()), payment.getId());
        
        log.info("🎉 Payment processed successfully: {}", razorpayPaymentId);
    }
    
    /**
     * 🔥 ACCEPT WORK: Release payment to worker immediately
     */
    @Transactional
    public void acceptWorkAndReleasePayment(Long taskId, Long clientUserId, String acceptanceReason) {
        log.info("✅ Client {} accepting work for task: {}", clientUserId, taskId);
        
        // 1. Find escrow
        Optional<Escrow> escrowOpt = escrowRepository.findByTaskId(taskId);
        if (escrowOpt.isEmpty()) {
            throw new RuntimeException("No escrow found for task: " + taskId);
        }
        
        Escrow escrow = escrowOpt.get();
        
        // 2. Validate escrow can be released
        if (!escrow.canBeReleased()) {
            throw new RuntimeException("Escrow cannot be released. Current status: " + escrow.getStatus());
        }
        
        // 3. Validate client is the owner
        if (!escrow.getClientUserId().equals(clientUserId)) {
            throw new RuntimeException("Only task owner can accept work");
        }
        
        // 4. Release payment to worker
        escrow.setStatus(EscrowStatus.RELEASED);
        escrow.setReleasedAt(LocalDateTime.now());
        escrow.setReleaseReason(acceptanceReason);
        escrowRepository.save(escrow);
        
        // 5. Add money to worker's wallet
        addEarningsToWorker(escrow.getWorkerUserId(), escrow.getWorkerAmount(), taskId, "Work accepted by client");
        
        // 6. Create platform fee transaction
        createTransaction(null, escrow.getPaymentId(), escrow.getId(), 
                         escrow.getPlatformFee(), TransactionType.PLATFORM_FEE, TransactionStatus.COMPLETED,
                         "Platform fee for task: " + taskId, null);
        
        // 7. Update task status to completed
        TaskStatusUpdateRequest statusUpdate = TaskStatusUpdateRequest.builder()
                .status("COMPLETED")
                .reason("Work accepted by client")
                .updatedBy(clientUserId)
                .build();
        taskServiceClient.updateTaskStatus(taskId, statusUpdate);
        
        // 8. Update profile earnings
        AddEarningsRequest earningsRequest = AddEarningsRequest.builder()
                .amount(escrow.getWorkerAmount())
                .taskId(taskId)
                .description("Earnings from completed task")
                .paymentReference(paymentRepository.findById(escrow.getPaymentId())
                        .map(Payment::getRazorpayPaymentId).orElse("N/A"))
                .build();
        profileServiceClient.updateUserEarnings(escrow.getWorkerUserId(), earningsRequest);
        
        log.info("🎉 Work accepted and payment released: Task={}, Worker={}, Amount=₹{}", 
                taskId, escrow.getWorkerUserId(), escrow.getWorkerAmount());
    }
    
    /**
     * 🔥 REJECT WORK: Refund payment to client immediately
     */
    @Transactional
    public void rejectWorkAndRefundPayment(Long taskId, Long clientUserId, String rejectionReason) {
        log.info("❌ Client {} rejecting work for task: {}", clientUserId, taskId);
        
        // 1. Find escrow
        Optional<Escrow> escrowOpt = escrowRepository.findByTaskId(taskId);
        if (escrowOpt.isEmpty()) {
            throw new RuntimeException("No escrow found for task: " + taskId);
        }
        
        Escrow escrow = escrowOpt.get();
        
        // 2. Validate escrow can be refunded
        if (!escrow.canBeRefunded()) {
            throw new RuntimeException("Escrow cannot be refunded. Current status: " + escrow.getStatus());
        }
        
        // 3. Validate client is the owner
        if (!escrow.getClientUserId().equals(clientUserId)) {
            throw new RuntimeException("Only task owner can reject work");
        }
        
        // 4. Refund payment to client
        refundEscrowToClient(escrow, rejectionReason);
        
        // 5. Reopen task for new bids
        taskServiceClient.reopenTask(taskId);
        
        log.info("💸 Work rejected and payment refunded: Task={}, Client={}, Amount=₹{}", 
                taskId, escrow.getClientUserId(), escrow.getTotalAmount());
    }
    
    /**
     * 🔥 AUTO-REFUND: When task deadline expires without acceptance
     */
    @Transactional
    public void processExpiredTaskDeadline(Long taskId) {
        log.info("⏰ Processing expired task deadline for task: {}", taskId);
        
        Optional<Escrow> escrowOpt = escrowRepository.findByTaskId(taskId);
        if (escrowOpt.isEmpty()) {
            log.warn("⚠️ No escrow found for expired task: {}", taskId);
            return;
        }
        
        Escrow escrow = escrowOpt.get();
        
        if (escrow.getStatus() == EscrowStatus.FUNDED) {
            // Auto-refund to client
            refundEscrowToClient(escrow, "Task deadline expired - Automatic refund");
            
            // Mark task as failed and reopen
            taskServiceClient.markTaskAsFailedAndReopen(taskId);
            
            log.info("⏰ Auto-refunded expired task: {}, Amount: ₹{}", taskId, escrow.getTotalAmount());
        }
    }
    
    /**
     * Helper method to refund escrow to client
     */
    private void refundEscrowToClient(Escrow escrow, String reason) {
        try {
            // 1. Update escrow status
            escrow.setStatus(EscrowStatus.REFUNDED);
            escrow.setRefundedAt(LocalDateTime.now());
            escrow.setRefundReason(reason);
            escrowRepository.save(escrow);
            
            // 2. Add money back to client's wallet
            addRefundToClient(escrow.getClientUserId(), escrow.getTotalAmount(), escrow.getTaskId(), reason);
            
            // 3. Create refund transaction
            createTransaction(escrow.getClientUserId(), escrow.getPaymentId(), escrow.getId(),
                             escrow.getTotalAmount(), TransactionType.PAYMENT_REFUNDED, TransactionStatus.COMPLETED,
                             reason, null);
            
            // 4. Create Razorpay refund if payment was made via Razorpay
            Payment payment = paymentRepository.findById(escrow.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found for escrow: " + escrow.getId()));
            if (payment != null && payment.getRazorpayPaymentId() != null) {
                try {
                    razorpayService.createRefund(payment.getRazorpayPaymentId(), escrow.getTotalAmount(), reason);
                    log.info("💸 Razorpay refund initiated for payment: {}", payment.getRazorpayPaymentId());
                } catch (RazorpayException e) {
                    log.error("❌ Failed to create Razorpay refund: {}", e.getMessage());
                    // Continue with internal refund even if Razorpay refund fails
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing refund for escrow {}: {}", escrow.getId(), e.getMessage());
            throw new RuntimeException("Failed to process refund", e);
        }
    }
    
    /**
     * Add earnings to worker's wallet
     */
    private void addEarningsToWorker(Long workerUserId, BigDecimal amount, Long taskId, String description) {
        // 1. Get or create worker wallet
        Wallet wallet = getOrCreateWallet(workerUserId);
        
        // 2. Add earnings
        wallet.addEarnings(amount);
        walletRepository.save(wallet);
        
        // 3. Create transaction record
        createTransaction(workerUserId, null, null, amount, 
                         TransactionType.EARNINGS_RECEIVED, TransactionStatus.COMPLETED,
                         description, "task_" + taskId);
        
        log.info("💰 Added ₹{} earnings to worker {} for task {}", amount, workerUserId, taskId);
    }
    
    /**
     * Add refund to client's wallet
     */
    private void addRefundToClient(Long clientUserId, BigDecimal amount, Long taskId, String reason) {
        // 1. Get or create client wallet
        Wallet wallet = getOrCreateWallet(clientUserId);
        
        // 2. Add refund
        wallet.addRefund(amount);
        walletRepository.save(wallet);
        
        // 3. Create transaction record
        createTransaction(clientUserId, null, null, amount, 
                         TransactionType.PAYMENT_REFUNDED, TransactionStatus.COMPLETED,
                         reason, "task_" + taskId);
        
        log.info("💸 Added ₹{} refund to client {} for task {}", amount, clientUserId, taskId);
    }
    
    /**
     * Update wallet spending when client makes payment
     */
    private void updateWalletSpending(Long clientUserId, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(clientUserId);
        wallet.addSpending(amount);
        walletRepository.save(wallet);
        
        log.info("💳 Updated spending for client {}: +₹{}", clientUserId, amount);
    }
    
    /**
     * Get or create wallet for user
     */
    private Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .userId(userId)
                            .balance(BigDecimal.ZERO)
                            .totalEarned(BigDecimal.ZERO)
                            .totalSpent(BigDecimal.ZERO)
                            .totalRefunded(BigDecimal.ZERO)
                            .status(WalletStatus.ACTIVE)
                            .build();
                    
                    Wallet saved = walletRepository.save(newWallet);
                    log.info("👛 Created new wallet for user: {}", userId);
                    return saved;
                });
    }
    
    /**
     * Create transaction record
     */
    private Transaction createTransaction(Long userId, Long paymentId, Long escrowId, BigDecimal amount,
                                        TransactionType type, TransactionStatus status, String description, String referenceId) {
        
        // Get current balance if user wallet exists
        BigDecimal balanceBefore = BigDecimal.ZERO;
        BigDecimal balanceAfter = BigDecimal.ZERO;
        
        if (userId != null) {
            Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
            if (walletOpt.isPresent()) {
                balanceBefore = walletOpt.get().getBalance();
                // Calculate balance after based on transaction type
                if (type == TransactionType.EARNINGS_RECEIVED || type == TransactionType.PAYMENT_REFUNDED) {
                    balanceAfter = balanceBefore.add(amount);
                } else if (type == TransactionType.PAYMENT_RECEIVED) {
                    balanceAfter = balanceBefore.subtract(amount);
                } else {
                    balanceAfter = balanceBefore;
                }
            }
        }
        
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .paymentId(paymentId)
                .escrowId(escrowId)
                .amount(amount)
                .type(type)
                .status(status)
                .description(description)
                .referenceId(referenceId)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .processedAt(status == TransactionStatus.COMPLETED ? LocalDateTime.now() : null)
                .build();
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Mark payment as failed
     */
    private void markPaymentAsFailed(Payment payment, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setFailedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        
        // Also mark escrow as refunded if exists
        escrowRepository.findByPaymentId(payment.getId()).ifPresent(escrow -> {
            escrow.setStatus(EscrowStatus.REFUNDED);
            escrow.setRefundedAt(LocalDateTime.now());
            escrow.setRefundReason(reason);
            escrowRepository.save(escrow);
        });
        
        log.error("❌ Payment marked as failed: {}, Reason: {}", payment.getId(), reason);
    }
    
    /**
     * Get winning bid ID for a task
     */
    private Long getWinningBidId(Long taskId) {
        try {
            BidResponse winningBid = biddingServiceClient.getWinningBid(taskId);
            return winningBid.getId();
        } catch (Exception e) {
            log.warn("⚠️ Could not get winning bid ID for task: {}", taskId);
            return null;
        }
    }
    
    /**
     * Validate transaction amount
     */
    private void validateTransactionAmount(BigDecimal amount) {
        if (amount.compareTo(minTransactionAmount) < 0) {
            throw new RuntimeException("Amount too low. Minimum: ₹" + minTransactionAmount);
        }
        if (amount.compareTo(maxTransactionAmount) > 0) {
            throw new RuntimeException("Amount too high. Maximum: ₹" + maxTransactionAmount);
        }
    }
    
    /**
     * Get payment details
     */
    public Payment getPaymentByTaskId(Long taskId) {
        return paymentRepository.findByTaskId(taskId)
                .orElseThrow(() -> new RuntimeException("No payment found for task: " + taskId));
    }
    
    /**
     * Get escrow details
     */
    public Escrow getEscrowByTaskId(Long taskId) {
        return escrowRepository.findByTaskId(taskId)
                .orElseThrow(() -> new RuntimeException("No escrow found for task: " + taskId));
    }
    
    /**
     * Get user wallet
     */
    public Wallet getUserWallet(Long userId) {
        return getOrCreateWallet(userId);
    }
    
    /**
     * Get user transactions
     */
    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
