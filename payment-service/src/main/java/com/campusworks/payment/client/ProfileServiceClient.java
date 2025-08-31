package com.campusworks.payment.client;

import com.campusworks.payment.config.FeignClientConfig;
import com.campusworks.payment.dto.AddEarningsRequest;
import com.campusworks.payment.dto.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "profile-service", configuration = FeignClientConfig.class, fallback = ProfileServiceClientFallback.class)
public interface ProfileServiceClient {
    
    @GetMapping("/profiles/user/{userId}")
    ProfileResponse getUserProfile(@PathVariable("userId") Long userId);
    
    @PostMapping("/profiles/{profileId}/earnings")
    ResponseEntity<?> addEarnings(@PathVariable("profileId") Long profileId,
                                @RequestBody AddEarningsRequest request);
    
    @PutMapping("/profiles/user/{userId}/earnings")
    ResponseEntity<?> updateUserEarnings(@PathVariable("userId") Long userId,
                                       @RequestBody AddEarningsRequest request);
}
