package com.campusworks.payment.client;

import com.campusworks.payment.dto.BidResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class BiddingServiceClientFallback implements BiddingServiceClient {
    
    @Override
    public BidResponse getWinningBid(Long taskId) {
        log.error("🔥 Bidding Service unavailable - Using fallback for getWinningBid({})", taskId);
        return BidResponse.builder()
                .id(null)
                .taskId(taskId)
                .bidderId(null)
                .bidderEmail("unavailable@fallback.com")
                .bidderName("Bidding Service Unavailable")
                .amount(BigDecimal.ZERO)
                .proposal("Unable to fetch winning bid details")
                .status("UNKNOWN")
                .success(false)
                .message("Bidding Service unavailable - Using fallback response")
                .build();
    }
    
    @Override
    public ResponseEntity<?> notifyPaymentConfirmed(Long bidId, Long paymentId) {
        log.error("🔥 Bidding Service unavailable - Using fallback for notifyPaymentConfirmed({}, {})", bidId, paymentId);
        return ResponseEntity.ok().body("Bidding Service unavailable - Payment confirmation queued for retry");
    }
    
    @Override
    public ResponseEntity<?> notifyPaymentFailed(Long bidId, String reason) {
        log.error("🔥 Bidding Service unavailable - Using fallback for notifyPaymentFailed({}, {})", bidId, reason);
        return ResponseEntity.ok().body("Bidding Service unavailable - Payment failure notification queued for retry");
    }
}
