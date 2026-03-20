package br.com.tisaicore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignBatchRequest(
        @NotEmpty(message = "Items are required")
        @Valid
        List<ItemBatch> items
) {
    public record ItemBatch(
            @NotNull(message = "Order item ID is required")
            Long orderItemId,

            @NotNull(message = "Batch ID is required")
            Long batchId
    ) {}
}
