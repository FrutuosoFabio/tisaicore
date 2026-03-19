package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.Batch;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BatchResponse(
        Long id,
        String code,
        Long productId,
        String productName,
        String productSku,
        LocalDate expirationDate,
        LocalDate manufacturingDate,
        String supplier,
        Integer initialQuantity,
        Integer currentQuantity,
        boolean expired,
        String notes,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BatchResponse from(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getCode(),
                batch.getProduct().getId(),
                batch.getProduct().getName(),
                batch.getProduct().getSku(),
                batch.getExpirationDate(),
                batch.getManufacturingDate(),
                batch.getSupplier(),
                batch.getInitialQuantity(),
                batch.getCurrentQuantity(),
                batch.isExpired(),
                batch.getNotes(),
                batch.isActive(),
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }
}
