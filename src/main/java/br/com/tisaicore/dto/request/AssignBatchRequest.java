package br.com.tisaicore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignBatchRequest(
        @NotEmpty(message = "Itens são obrigatórios")
        @Valid
        List<ItemBatch> items
) {
    public record ItemBatch(
            @NotNull(message = "ID do item do pedido é obrigatório")
            Long orderItemId,

            @NotNull(message = "ID do lote é obrigatório")
            Long batchId
    ) {}
}
