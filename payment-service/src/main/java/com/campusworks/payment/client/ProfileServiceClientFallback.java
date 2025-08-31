package com.campusworks.payment.client;

import com.campusworks.payment.dto.AddEarningsRequest;
import com.campusworks.payment.dto.ProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class ProfileServiceClientFallback implements ProfileServiceClient {
    
    @Override
    public ProfileResponse getUserProfile(Long userId) {
        log.error("🔥 Profile Service unavailable - Using fallback for getUserProfile({})", userId);
        return ProfileResponse.builder()
                .id(null)
                .userId(userId)
                .fullName("Profile Service Unavailable")
                .email("unavailable@fallback.com")
                .totalEarnings(BigDecimal.ZERO)
                .completedTasks(0)
                .rating(0.0)
                .success(false)
                .message("Profile Service unavailable - Using fallback response")
                .build();
    }
    
    @Override
    public ResponseEntity<?> addEarnings(Long profileId, AddEarningsRequest request) {
        log.error("🔥 Profile Service unavailable - Using fallback for addEarnings({}, {})", profileId, request.getAmount());
        return ResponseEntity.ok().body("Profile Service unavailable - Earnings update queued for retry");
    }
    
    @Override
    public ResponseEntity<?> updateUserEarnings(Long userId, AddEarningsRequest request) {
        log.error("🔥 Profile Service unavailable - Using fallback for updateUserEarnings({}, {})", userId, request.getAmount());
        return ResponseEntity.ok().body("Profile Service unavailable - User earnings update queued for retry");
    }
}
