package br.com.tisaicore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateBatchRequest(
        String code,

        @NotNull(message = "Produto é obrigatório")
        Long productId,

        LocalDate expirationDate,

        LocalDate manufacturingDate,

        String supplier,

        @NotNull(message = "Quantidade inicial é obrigatória")
        @Positive(message = "Quantidade inicial deve ser positiva")
        Integer initialQuantity,

        String notes
) {}
