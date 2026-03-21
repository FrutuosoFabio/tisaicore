package br.com.tisaicore.dto.request;

import br.com.tisaicore.entity.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementRequest(
        @NotNull(message = "Produto é obrigatório")
        Long productId,

        @NotNull(message = "Tipo de movimentação é obrigatório")
        MovementType type,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser positiva")
        Integer quantity,

        String reason,

        Long batchId
) {}
