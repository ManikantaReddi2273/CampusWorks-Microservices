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
public class BidResponse {
    private Long id;
    private Long taskId;
    private Long bidderId;
    private String bidderEmail;
    private String bidderName;
    private BigDecimal amount;
    private String proposal;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private boolean success;
    private String message;
}
