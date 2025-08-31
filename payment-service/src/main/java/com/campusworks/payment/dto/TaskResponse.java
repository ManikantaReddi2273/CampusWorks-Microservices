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
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal budget;
    private String status;
    private Long ownerId;
    private String ownerEmail;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime biddingDeadline;
    private LocalDateTime taskDeadline; // 🔥 KEY: This is what we use for escrow expiration
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private String category;
    private String priority;
    private String location;
    private boolean success;
    private String message;
}
