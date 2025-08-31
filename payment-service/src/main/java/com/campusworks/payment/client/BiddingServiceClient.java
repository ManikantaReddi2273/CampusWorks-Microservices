package com.campusworks.payment.client;

import com.campusworks.payment.config.FeignClientConfig;
import com.campusworks.payment.dto.BidResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "bidding-service", configuration = FeignClientConfig.class, fallback = BiddingServiceClientFallback.class)
public interface BiddingServiceClient {
    
    @GetMapping("/bids/task/{taskId}/winning")
    BidResponse getWinningBid(@PathVariable("taskId") Long taskId);
    
    @PostMapping("/bids/{bidId}/payment-confirmed")
    ResponseEntity<?> notifyPaymentConfirmed(@PathVariable("bidId") Long bidId,
                                           @RequestParam("paymentId") Long paymentId);
    
    @PostMapping("/bids/{bidId}/payment-failed")
    ResponseEntity<?> notifyPaymentFailed(@PathVariable("bidId") Long bidId,
                                        @RequestParam("reason") String reason);
}
