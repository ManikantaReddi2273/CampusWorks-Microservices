package com.campusworks.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEarningsRequest {
    private BigDecimal amount;
    private Long taskId;
    private String description;
    private String paymentReference;
}
