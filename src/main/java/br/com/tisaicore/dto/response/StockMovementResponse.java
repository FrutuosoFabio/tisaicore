package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.MovementType;
import br.com.tisaicore.entity.StockMovement;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long productId,
        String productName,
        MovementType type,
        Integer quantity,
        String reason,
        String userName,
        LocalDateTime createdAt
) {
    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getType(),
                movement.getQuantity(),
                movement.getReason(),
                movement.getUser().getName(),
                movement.getCreatedAt()
        );
    }
}
