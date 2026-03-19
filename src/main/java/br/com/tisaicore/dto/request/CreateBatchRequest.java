package br.com.tisaicore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateBatchRequest(
        @NotBlank(message = "Batch code is required")
        String code,

        @NotNull(message = "Product ID is required")
        Long productId,

        LocalDate expirationDate,

        LocalDate manufacturingDate,

        String supplier,

        @NotNull(message = "Initial quantity is required")
        @Positive(message = "Initial quantity must be positive")
        Integer initialQuantity,

        String notes
) {}
