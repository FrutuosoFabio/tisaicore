package br.com.tisaicore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "Empresa é obrigatória")
        Long companyId,

        @NotEmpty(message = "Pedido deve ter pelo menos um item")
        @Valid
        List<OrderItemRequest> items,

        String notes
) {
    public record OrderItemRequest(
            @NotNull(message = "Produto é obrigatório")
            Long productId,

            @NotNull(message = "Quantidade é obrigatória")
            @Positive(message = "Quantidade deve ser positiva")
            Integer quantity
    ) {}
}
