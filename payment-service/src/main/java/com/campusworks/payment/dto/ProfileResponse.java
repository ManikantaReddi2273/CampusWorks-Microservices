package com.campusworks.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String bio;
    private String skills;
    private String experience;
    private String education;
    private String location;
    private BigDecimal totalEarnings;
    private Integer completedTasks;
    private Double rating;
    private Integer totalRatings;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean success;
    private String message;
}
