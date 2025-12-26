package com.example.Al.Baraka.dto.response;

import com.example.Al.Baraka.enums.OperationStatus;
import com.example.Al.Baraka.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationResponse {

    private Long id;
    private OperationType type;
    private BigDecimal amount;
    private OperationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
    private LocalDateTime executedAt;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String message;
    private Boolean requiresDocument;
    private Boolean hasDocument;
}
