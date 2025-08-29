package com.campusworks.bidding.service;

import com.campusworks.bidding.client.TaskServiceClient;
import com.campusworks.bidding.model.Bid;
import com.campusworks.bidding.repo.BidRepository;
import com.campusworks.bidding.dto.TaskStatusUpdateRequest;
import com.campusworks.bidding.dto.BiddingStatusResponse;
import com.campusworks.bidding.dto.TaskUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import com.campusworks.bidding.dto.TaskAssignmentRequest;
import com.campusworks.bidding.dto.TaskOwnershipResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;

/**
 * Bidding Service
 * Handles business logic for bid management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BiddingService {
    
    private final BidRepository bidRepository;
    
    @Autowired
    private TaskServiceClient taskServiceClient;
    
    @Value("${bidding.min-amount:0.01}")
    private BigDecimal minBidAmount;
    
    @Value("${bidding.max-amount:10000.00}")
    private BigDecimal maxBidAmount;
    
    @Value("${bidding.auto-assignment-enabled:true}")
    private boolean autoAssignmentEnabled;
    
    @Value("${bidding.notification-enabled:true}")
    private boolean notificationEnabled;
    
    @Value("${bidding.auto-assignment-check-interval:300000}")
    private long autoAssignmentCheckInterval;
    
    /**
     * Place a new bid on a task
     */
    public Bid placeBid(Bid bid) {
        log.info("💰 Placing bid on task ID: {} by user: {} ({}) for amount: ${}", 
                bid.getTaskId(), bid.getBidderEmail(), bid.getBidderId(), bid.getAmount());
        
        try {
            // Validate task exists and is open for bidding via Task Service
            boolean taskExists = taskServiceClient.checkTaskExists(bid.getTaskId());
            if (!taskExists) {
                log.error("❌ Task ID: {} does not exist", bid.getTaskId());
                throw new RuntimeException("Task not found");
            }
            
            // 🚨 OWNER BIDDING RESTRICTION: Prevent task owners from bidding on their own tasks
            log.info("🔍 Calling Task Service to check ownership for task {} and user {}", bid.getTaskId(), bid.getBidderId());
            
            boolean isOwner = false;
            try {
                TaskOwnershipResponse ownershipResponse = taskServiceClient.isTaskOwner(bid.getTaskId(), bid.getBidderId());
                
                log.info("🔍 Ownership response received: {}", ownershipResponse);
                log.info("🔍 Response details - taskId: {}, userId: {}, isOwner: {}, message: {}, success: {}", 
                        ownershipResponse.getTaskId(), 
                        ownershipResponse.getUserId(), 
                        ownershipResponse.isOwner(), 
                        ownershipResponse.getMessage(), 
                        ownershipResponse.isSuccess());
                
                isOwner = ownershipResponse.isOwner();
                
            } catch (Exception e) {
                log.error("❌ Error during ownership check via Feign Client: {}", e.getMessage());
                log.error("❌ Full error details: ", e);
                
                // If Feign Client fails, assume user IS owner for safety
                log.warn("⚠️ Assuming user IS owner for safety due to Feign Client failure");
                isOwner = true;
            }
            
            if (isOwner) {
                log.error("❌ BLOCKED: User {} (ID: {}) attempted to bid on their own task ID: {}", 
                        bid.getBidderEmail(), bid.getBidderId(), bid.getTaskId());
                throw new RuntimeException("Task owners cannot bid on their own tasks. This creates a conflict of interest and is not allowed.");
            }
            
            log.info("✅ Owner validation passed: User {} is not the owner of task {}", bid.getBidderEmail(), bid.getTaskId());
            
            // Get task bidding status from Task Service
            BiddingStatusResponse biddingStatusResponse = taskServiceClient.getTaskBiddingStatus(bid.getTaskId());
            
            if (biddingStatusResponse == null) {
                log.error("❌ Failed to get bidding status for task ID: {}", bid.getTaskId());
                throw new RuntimeException("Failed to get task bidding status");
            }
            
            boolean isOpenForBidding = biddingStatusResponse.isOpenForBidding();
            String status = biddingStatusResponse.getStatus();
            LocalDateTime biddingDeadline = biddingStatusResponse.getBiddingDeadline();
            
            if (!isOpenForBidding) {
                log.error("❌ Task ID: {} is not open for bidding - Status: {}, Bidding Deadline: {}", 
                    bid.getTaskId(), status, biddingDeadline);
                
                // Provide more specific error message
                String errorMessage = "Task is not open for bidding";
                if (biddingDeadline != null) {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(biddingDeadline)) {
                        errorMessage = "Bidding period has expired. Bidding deadline was: " + 
                                     biddingDeadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                }
                throw new RuntimeException(errorMessage);
            }
            
            // Validate bid data
            validateBidData(bid);
            
            // Check if user has already bid on this task
            if (bidRepository.existsByTaskIdAndBidderId(bid.getTaskId(), bid.getBidderId())) {
                log.warn("❌ User {} has already bid on task ID: {}", bid.getBidderEmail(), bid.getTaskId());
                throw new RuntimeException("You have already placed a bid on this task");
            }
            
            // Set default values
            bid.setStatus(Bid.BidStatus.PENDING);
            bid.setIsWinning(false);
            bid.setIsAccepted(false);
            bid.setCreatedAt(LocalDateTime.now());
            bid.setUpdatedAt(LocalDateTime.now());
            
            // Save bid
            Bid savedBid = bidRepository.save(bid);
            
            log.info("✅ Bid placed successfully: ID: {}, Amount: ${}, Task: {}", 
                    savedBid.getId(), savedBid.getAmount(), savedBid.getTaskId());
            
            // Check if this is the lowest bid and update winning status
            updateWinningBidStatus(bid.getTaskId());
            
            return savedBid;
            
        } catch (Exception e) {
            log.error("❌ Error placing bid on task ID: {}. Error: {}", bid.getTaskId(), e.getMessage());
            throw new RuntimeException("Failed to place bid: " + e.getMessage());
        }
    }
    
    /**
     * Get bid by ID
     */
    public Optional<Bid> getBidById(Long id) {
        log.info("🔍 Retrieving bid with ID: {}", id);
        
        Optional<Bid> bid = bidRepository.findById(id);
        
        if (bid.isPresent()) {
            log.info("✅ Bid found: ID: {}, Amount: ${}, Status: {}", 
                    bid.get().getId(), bid.get().getAmount(), bid.get().getStatus());
        } else {
            log.warn("❌ Bid not found with ID: {}", id);
        }
        
        return bid;
    }
    
    /**
     * Get all bids for a task
     */
    public List<Bid> getBidsByTaskId(Long taskId) {
        log.info("📋 Retrieving all bids for task ID: {}", taskId);
        
        List<Bid> bids = bidRepository.findByTaskIdOrderByAmountAsc(taskId);
        
        log.info("✅ Retrieved {} bids for task ID: {}", bids.size(), taskId);
        
        return bids;
    }
    
    /**
     * Get all bids by a user
     */
    public List<Bid> getBidsByUserId(Long userId) {
        log.info("👤 Retrieving all bids by user ID: {}", userId);
        
        List<Bid> bids = bidRepository.findByBidderIdOrderByCreatedAtDesc(userId);
        
        log.info("✅ Retrieved {} bids by user ID: {}", bids.size(), userId);
        
        return bids;
    }
    
    /**
     * Get all bids by user email
     */
    public List<Bid> getBidsByUserEmail(String userEmail) {
        log.info("📧 Retrieving all bids by user email: {}", userEmail);
        
        List<Bid> bids = bidRepository.findByBidderEmailOrderByCreatedAtDesc(userEmail);
        
        log.info("✅ Retrieved {} bids by user email: {}", bids.size(), userEmail);
        
        return bids;
    }
    
    /**
     * Get all bids by status
     */
    public List<Bid> getBidsByStatus(Bid.BidStatus status) {
        log.info("🏷️ Retrieving bids with status: {}", status);
        
        List<Bid> bids = bidRepository.findByStatusOrderByCreatedAtDesc(status);
        
        log.info("✅ Retrieved {} bids with status: {}", bids.size(), status);
        
        return bids;
    }
    
    /**
     * Get winning bid for a task
     */
    public Optional<Bid> getWinningBidForTask(Long taskId) {
        log.info("🏆 Retrieving winning bid for task ID: {}", taskId);
        
        Optional<Bid> winningBid = bidRepository.findByTaskIdAndIsWinningTrue(taskId);
        
        if (winningBid.isPresent()) {
            log.info("✅ Winning bid found: ID: {}, Amount: ${}, Bidder: {}", 
                    winningBid.get().getId(), winningBid.get().getAmount(), winningBid.get().getBidderEmail());
        } else {
            log.info("ℹ️ No winning bid found for task ID: {}", taskId);
        }
        
        return winningBid;
    }
    
    /**
     * Get lowest bid for a task
     */
    public Optional<Bid> getLowestBidForTask(Long taskId) {
        log.info("💰 Retrieving lowest bid for task ID: {}", taskId);
        
        Optional<Bid> lowestBid = bidRepository.findLowestBidForTask(taskId);
        
        if (lowestBid.isPresent()) {
            log.info("✅ Lowest bid found: ID: {}, Amount: ${}, Bidder: {}", 
                    lowestBid.get().getId(), lowestBid.get().getAmount(), lowestBid.get().getBidderEmail());
        } else {
            log.info("ℹ️ No bids found for task ID: {}", taskId);
        }
        
        return lowestBid;
    }
    
    /**
     * Get highest bid for a task
     */
    public Optional<Bid> getHighestBidForTask(Long taskId) {
        log.info("💎 Retrieving highest bid for task ID: {}", taskId);
        
        Optional<Bid> highestBid = bidRepository.findHighestBidForTask(taskId);
        
        if (highestBid.isPresent()) {
            log.info("✅ Highest bid found: ID: {}, Amount: ${}, Bidder: {}", 
                    highestBid.get().getId(), highestBid.get().getAmount(), highestBid.get().getBidderEmail());
        } else {
            log.info("ℹ️ No bids found for task ID: {}", taskId);
        }
        
        return highestBid;
    }
    
    /**
     * Accept a bid
     */
    public Bid acceptBid(Long bidId, Long taskOwnerId) {
        log.info("👍 Accepting bid ID: {} by task owner: {}", bidId, taskOwnerId);
        
        try {
            Optional<Bid> bidOpt = bidRepository.findById(bidId);
            
            if (bidOpt.isEmpty()) {
                log.warn("❌ Bid not found with ID: {}", bidId);
                throw new RuntimeException("Bid not found");
            }
            
            Bid bid = bidOpt.get();
            
            // Check if bid is still pending
            if (!bid.isPending()) {
                log.warn("❌ Bid ID: {} cannot be accepted - status is: {}", bidId, bid.getStatus());
                throw new RuntimeException("Bid cannot be accepted - it is not pending");
            }
            
            // Accept the bid
            bid.acceptBid();
            
            // Mark all other bids for this task as rejected
            rejectOtherBidsForTask(bid.getTaskId(), bidId);
            
            Bid savedBid = bidRepository.save(bid);
            
            // Update task status in Task Service via Feign Client
            TaskUpdateResponse updateRequest = TaskUpdateResponse.builder()
                .taskId(bid.getTaskId())
                .status("ASSIGNED")
                .message("Bid accepted: " + bid.getProposal())
                .updatedAt(LocalDateTime.now())
                .success(true)
                .build();
            
            try {
                taskServiceClient.updateTaskStatus(bid.getTaskId(), updateRequest);
                log.info("✅ Task status updated in Task Service for task ID: {}", bid.getTaskId());
            } catch (Exception e) {
                log.warn("⚠️ Failed to update task status in Task Service: {}", e.getMessage());
                // Continue with bid acceptance even if task service update fails
            }
            
            log.info("✅ Bid accepted successfully: ID: {}, Amount: ${}, Bidder: {}", 
                    savedBid.getId(), savedBid.getAmount(), savedBid.getBidderEmail());
            
            return savedBid;
            
        } catch (Exception e) {
            log.error("❌ Error accepting bid ID: {}. Error: {}", bidId, e.getMessage());
            throw new RuntimeException("Failed to accept bid: " + e.getMessage());
        }
    }
    
    /**
     * Reject a bid
     */
    public Bid rejectBid(Long bidId, String rejectionReason) {
        log.info("❌ Rejecting bid ID: {} with reason: {}", bidId, rejectionReason);
        
        Optional<Bid> bidOpt = bidRepository.findById(bidId);
        
        if (bidOpt.isEmpty()) {
            log.warn("❌ Bid not found with ID: {}", bidId);
            throw new RuntimeException("Bid not found");
        }
        
        Bid bid = bidOpt.get();
        
        // Check if bid is still pending
        if (!bid.isPending()) {
            log.warn("❌ Bid ID: {} cannot be rejected - status is: {}", bidId, bid.getStatus());
            throw new RuntimeException("Bid cannot be rejected - it is not pending");
        }
        
        // Reject the bid
        bid.rejectBid(rejectionReason);
        
        Bid savedBid = bidRepository.save(bid);
        
        log.info("✅ Bid rejected successfully: ID: {}, Reason: {}", savedBid.getId(), rejectionReason);
        
        return savedBid;
    }
    
    /**
     * Withdraw a bid
     */
    public Bid withdrawBid(Long bidId, Long bidderId) {
        log.info("↩️ Withdrawing bid ID: {} by bidder: {}", bidId, bidderId);
        
        Optional<Bid> bidOpt = bidRepository.findById(bidId);
        
        if (bidOpt.isEmpty()) {
            log.warn("❌ Bid not found with ID: {}", bidId);
            throw new RuntimeException("Bid not found");
        }
        
        Bid bid = bidOpt.get();
        
        // Check if bidder owns this bid
        if (!bid.getBidderId().equals(bidderId)) {
            log.warn("❌ User {} is not authorized to withdraw bid ID: {}", bidderId, bidId);
            throw new RuntimeException("You are not authorized to withdraw this bid");
        }
        
        // Check if bid is still pending
        if (!bid.isPending()) {
            log.warn("❌ Bid ID: {} cannot be withdrawn - status is: {}", bidId, bid.getStatus());
            throw new RuntimeException("Bid cannot be withdrawn - it is not pending");
        }
        
        // Withdraw the bid
        bid.withdrawBid();
        
        Bid savedBid = bidRepository.save(bid);
        
        log.info("✅ Bid withdrawn successfully: ID: {}, Bidder: {}", savedBid.getId(), savedBid.getBidderEmail());
        
        // Update winning bid status for the task
        updateWinningBidStatus(bid.getTaskId());
        
        return savedBid;
    }
    
    /**
     * Update winning bid status for a task
     */
    public void updateWinningBidStatus(Long taskId) {
        log.info("🏆 Updating winning bid status for task ID: {}", taskId);
        
        // Get all pending bids for the task
        List<Bid> pendingBids = bidRepository.findByTaskIdAndStatusOrderByAmountAsc(taskId, Bid.BidStatus.PENDING);
        
        if (pendingBids.isEmpty()) {
            log.info("ℹ️ No pending bids found for task ID: {}", taskId);
            return;
        }
        
        // Find the lowest bid
        Bid lowestBid = pendingBids.get(0);
        
        // Mark all bids as not winning first
        for (Bid bid : pendingBids) {
            if (bid.getIsWinning()) {
                bid.markAsNotWinning();
                bidRepository.save(bid);
            }
        }
        
        // Mark the lowest bid as winning
        lowestBid.markAsWinning();
        bidRepository.save(lowestBid);
        
        log.info("✅ Winning bid updated: ID: {}, Amount: ${}, Bidder: {}", 
                lowestBid.getId(), lowestBid.getAmount(), lowestBid.getBidderEmail());
    }
    
    /**
     * Get bids that need attention
     */
    public List<Bid> getBidsNeedingAttention() {
        log.info("⚠️ Retrieving bids that need attention");
        
        List<Bid> bids = bidRepository.findBidsNeedingAttention(LocalDateTime.now());
        
        log.info("✅ Retrieved {} bids that need attention", bids.size());
        
        return bids;
    }
    
    /**
     * Get user's active bids
     */
    public List<Bid> getUserActiveBids(Long userId) {
        log.info("👤 Retrieving active bids for user ID: {}", userId);
        
        List<Bid> activeBids = bidRepository.findActiveBidsByUser(userId);
        
        log.info("✅ Retrieved {} active bids for user ID: {}", activeBids.size(), userId);
        
        return activeBids;
    }
    
    /**
     * Get user's completed bids
     */
    public List<Bid> getUserCompletedBids(Long userId) {
        log.info("✅ Retrieving completed bids for user ID: {}", userId);
        
        List<Bid> completedBids = bidRepository.findCompletedBidsByUser(userId);
        
        log.info("✅ Retrieved {} completed bids for user ID: {}", completedBids.size(), userId);
        
        return completedBids;
    }
    
    /**
     * Get bid statistics
     */
    public BidStatistics getBidStatistics() {
        log.info("📊 Retrieving bid statistics");
        
        long totalBids = bidRepository.count();
        long pendingBids = bidRepository.countByStatus(Bid.BidStatus.PENDING);
        long acceptedBids = bidRepository.countByStatus(Bid.BidStatus.ACCEPTED);
        long rejectedBids = bidRepository.countByStatus(Bid.BidStatus.REJECTED);
        long withdrawnBids = bidRepository.countByStatus(Bid.BidStatus.WITHDRAWN);
        long winningBids = bidRepository.countByIsWinningTrue();
        
        BidStatistics stats = BidStatistics.builder()
                .totalBids(totalBids)
                .pendingBids(pendingBids)
                .acceptedBids(acceptedBids)
                .rejectedBids(rejectedBids)
                .withdrawnBids(withdrawnBids)
                .winningBids(winningBids)
                .build();
        
        log.info("✅ Bid statistics retrieved: {}", stats);
        
        return stats;
    }
    
    /**
     * Reject all other bids for a task when one is accepted
     */
    private void rejectOtherBidsForTask(Long taskId, Long acceptedBidId) {
        log.info("❌ Rejecting all other bids for task ID: {} (accepted bid: {})", taskId, acceptedBidId);
        
        List<Bid> otherBids = bidRepository.findByTaskIdAndStatusOrderByAmountAsc(taskId, Bid.BidStatus.PENDING);
        
        for (Bid bid : otherBids) {
            if (!bid.getId().equals(acceptedBidId)) {
                bid.rejectBid("Another bid was accepted for this task");
                bidRepository.save(bid);
                log.info("✅ Rejected bid ID: {} for task ID: {}", bid.getId(), taskId);
            }
        }
    }
    
    /**
     * Validate bid data
     */
    private void validateBidData(Bid bid) {
        if (bid.getTaskId() == null) {
            throw new RuntimeException("Task ID is required");
        }
        
        if (bid.getBidderId() == null) {
            throw new RuntimeException("Bidder ID is required");
        }
        
        if (bid.getBidderEmail() == null || bid.getBidderEmail().trim().isEmpty()) {
            throw new RuntimeException("Bidder email is required");
        }
        
        if (bid.getAmount() == null) {
            throw new RuntimeException("Bid amount is required");
        }
        
        if (bid.getAmount().compareTo(minBidAmount) < 0) {
            throw new RuntimeException("Bid amount must be at least $" + minBidAmount);
        }
        
        if (bid.getAmount().compareTo(maxBidAmount) > 0) {
            throw new RuntimeException("Bid amount cannot exceed $" + maxBidAmount);
        }
    }
    
    /**
     * Bid Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class BidStatistics {
        private long totalBids;
        private long pendingBids;
        private long acceptedBids;
        private long rejectedBids;
        private long withdrawnBids;
        private long winningBids;
    }

    /**
     * Check if notifications are enabled
     */
    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }
    
    /**
     * Get tasks that are ready for automatic bid selection
     * (Tasks with expired bidding deadlines that are still open)
     */
    public List<Long> getTasksReadyForAutomaticBidSelection() {
        log.info("📋 Getting tasks ready for automatic bid selection");
        
        try {
            // Get all task IDs that have pending bids
            List<Long> taskIdsWithPendingBids = bidRepository.findAllTaskIdsWithPendingBids();
            
            if (taskIdsWithPendingBids.isEmpty()) {
                log.info("ℹ️ No tasks with pending bids found");
                return new ArrayList<>();
            }
            
            log.info("📅 Found {} tasks with pending bids, checking for expired deadlines", taskIdsWithPendingBids.size());
            
            // Filter tasks that have expired bidding deadlines
            List<Long> readyTaskIds = new ArrayList<>();
            for (Long taskId : taskIdsWithPendingBids) {
                try {
                    if (isTaskBiddingDeadlineExpired(taskId)) {
                        readyTaskIds.add(taskId);
                        log.debug("✅ Task ID: {} is ready for automatic bid selection", taskId);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error checking task ID: {} for readiness. Error: {}", taskId, e.getMessage());
                    // Continue with other tasks
                }
            }
            
            log.info("📋 Found {} tasks ready for automatic bid selection", readyTaskIds.size());
            return readyTaskIds;
            
        } catch (Exception e) {
            log.error("❌ Error getting tasks ready for automatic bid selection: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // ==================== AUTOMATIC BID SELECTION ====================
    
    /**
     * Scheduled job to automatically select winning bids for expired bidding deadlines
     * Runs every 5 minutes by default
     */
    @Scheduled(fixedDelayString = "${bidding.auto-assignment-check-interval:300000}")
    public void processExpiredBiddingDeadlines() {
        if (!autoAssignmentEnabled) {
            log.debug("🔄 Automatic bid assignment is disabled, skipping scheduled job");
            return;
        }
        
        log.info("🔄 Starting scheduled job: Processing expired bidding deadlines");
        
        try {
            // Get all task IDs that have pending bids
            List<Long> taskIdsWithPendingBids = bidRepository.findAllTaskIdsWithPendingBids();
            
            if (taskIdsWithPendingBids.isEmpty()) {
                log.debug("ℹ️ No tasks with pending bids found");
                return;
            }
            
            log.info("📅 Found {} tasks with pending bids, checking for expired deadlines", taskIdsWithPendingBids.size());
            
            // Process each task to check if bidding deadline has expired
            for (Long taskId : taskIdsWithPendingBids) {
                try {
                    // Check if this task's bidding deadline has expired via Task Service
                    if (isTaskBiddingDeadlineExpired(taskId)) {
                        log.info("⏰ Task ID: {} bidding deadline has expired, processing automatic bid selection", taskId);
                        processExpiredBiddingDeadline(taskId);
                    } else {
                        log.debug("⏰ Task ID: {} bidding deadline has not expired yet", taskId);
                    }
                } catch (Exception e) {
                    log.error("❌ Error checking task ID: {} for expired deadline. Error: {}", 
                            taskId, e.getMessage(), e);
                    // Continue with other tasks even if one fails
                }
            }
            
            log.info("✅ Completed checking {} tasks for expired bidding deadlines", taskIdsWithPendingBids.size());
            
        } catch (Exception e) {
            log.error("❌ Error in scheduled job for processing expired bidding deadlines: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check if a task's bidding deadline has expired by calling Task Service
     */
    private boolean isTaskBiddingDeadlineExpired(Long taskId) {
        try {
            BiddingStatusResponse biddingStatus = taskServiceClient.getTaskBiddingStatus(taskId);
            if (biddingStatus != null && biddingStatus.getBiddingDeadline() != null) {
                LocalDateTime now = LocalDateTime.now();
                boolean isExpired = now.isAfter(biddingStatus.getBiddingDeadline());
                log.debug("⏰ Task ID: {} bidding deadline: {}, current time: {}, expired: {}", 
                        taskId, biddingStatus.getBiddingDeadline(), now, isExpired);
                return isExpired;
            }
            log.warn("⚠️ Could not determine bidding deadline for task ID: {}", taskId);
            return false;
        } catch (Exception e) {
            log.error("❌ Error checking bidding deadline for task ID: {}. Error: {}", 
                    taskId, e.getMessage(), e);
            return false; // Assume not expired if we can't check
        }
    }
    
    /**
     * Process expired bidding deadline for a specific task
     * Automatically selects the lowest bidder and assigns the task
     */
    @Transactional
    public void processExpiredBiddingDeadline(Long taskId) {
        log.info("🎯 Processing expired bidding deadline for task ID: {}", taskId);
        
        try {
            // Get all pending bids for the task, ordered by amount ASC, created_at ASC (tie-breaking)
            List<Bid> pendingBids = bidRepository.findPendingBidsForTaskOrderedByAmountAndTime(taskId);
            
            if (pendingBids.isEmpty()) {
                log.info("ℹ️ No pending bids found for task ID: {}, skipping automatic assignment", taskId);
                return;
            }
            
            log.info("💰 Found {} pending bids for task ID: {}", pendingBids.size(), taskId);
            
            // Select the winning bid (lowest amount, earliest time for tie-breaking)
            Bid winningBid = pendingBids.get(0);
            
            log.info("🏆 Automatic winner selected: Bid ID: {}, Amount: ${}, Bidder: {} ({}), Created: {}", 
                    winningBid.getId(), winningBid.getAmount(), winningBid.getBidderEmail(), 
                    winningBid.getBidderId(), winningBid.getCreatedAt());
            
            // Accept the winning bid
            winningBid.acceptBid();
            winningBid.setIsWinning(true);
            winningBid.setIsAccepted(true);
            bidRepository.save(winningBid);
            
            log.info("✅ Winning bid accepted: ID: {}, Status: {}", winningBid.getId(), winningBid.getStatus());
            
            // Reject all other bids for this task
            for (int i = 1; i < pendingBids.size(); i++) {
                Bid losingBid = pendingBids.get(i);
                losingBid.rejectBid("Automatic rejection: Another bid was selected as winner");
                losingBid.setIsWinning(false);
                bidRepository.save(losingBid);
                
                log.info("❌ Bid rejected: ID: {}, Amount: ${}, Bidder: {}, Reason: {}", 
                        losingBid.getId(), losingBid.getAmount(), losingBid.getBidderEmail(), 
                        losingBid.getRejectionReason());
            }
            
            // Automatically assign the task to the winning bidder via Task Service
            assignTaskToWinningBidder(taskId, winningBid);
            
            log.info("🎉 Task ID: {} automatically assigned to winning bidder: {} (${})", 
                    taskId, winningBid.getBidderEmail(), winningBid.getAmount());
            
        } catch (Exception e) {
            log.error("❌ Error processing expired bidding deadline for task ID: {}. Error: {}", 
                    taskId, e.getMessage(), e);
            throw new RuntimeException("Failed to process expired bidding deadline: " + e.getMessage(), e);
        }
    }
    
    /**
     * Automatically assign task to winning bidder via Task Service
     */
    private void assignTaskToWinningBidder(Long taskId, Bid winningBid) {
        log.info("🔗 Assigning task ID: {} to winning bidder: {} via Task Service", 
                taskId, winningBid.getBidderEmail());
        
        try {
            // Create task assignment request
            TaskAssignmentRequest assignmentRequest = TaskAssignmentRequest.builder()
                    .taskId(taskId)
                    .assignedUserId(winningBid.getBidderId())
                    .assignedUserEmail(winningBid.getBidderEmail())
                    .assignedAt(LocalDateTime.now())
                    .assignmentReason("Automatic assignment: Lowest bidder selected after bidding deadline expired")
                    .winningBidAmount(winningBid.getAmount())
                    .winningBidProposal(winningBid.getProposal())
                    .status("ASSIGNED")
                    .message("Task automatically assigned to lowest bidder")
                    .success(true)
                    .build();
            
            // Call Task Service to assign the task
            try {
                taskServiceClient.assignTask(taskId, assignmentRequest);
                log.info("✅ Task successfully assigned via Task Service for task ID: {}", taskId);
            } catch (Exception e) {
                log.error("❌ Failed to assign task via Task Service for task ID: {}. Error: {}", 
                        taskId, e.getMessage(), e);
                // Even if Task Service fails, the bid is still accepted
                // The task can be manually assigned later if needed
            }
            
        } catch (Exception e) {
            log.error("❌ Error creating task assignment request for task ID: {}. Error: {}", 
                    taskId, e.getMessage(), e);
            throw new RuntimeException("Failed to create task assignment request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Manually trigger automatic bid selection for a specific task
     * Useful for testing or manual intervention
     */
    public void manuallyTriggerBidSelection(Long taskId) {
        log.info("🔧 Manually triggering bid selection for task ID: {}", taskId);
        processExpiredBiddingDeadline(taskId);
    }
    
    /**
     * Check if automatic assignment is enabled
     */
    public boolean isAutoAssignmentEnabled() {
        return autoAssignmentEnabled;
    }
    
    /**
     * Get automatic assignment check interval
     */
    public long getAutoAssignmentCheckInterval() {
        return autoAssignmentCheckInterval;
    }
}
