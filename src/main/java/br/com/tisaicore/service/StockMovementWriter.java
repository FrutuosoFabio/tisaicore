package br.com.tisaicore.service;

import br.com.tisaicore.entity.Batch;
import br.com.tisaicore.entity.MovementType;
import br.com.tisaicore.entity.Product;
import br.com.tisaicore.entity.StockMovement;
import br.com.tisaicore.entity.User;
import br.com.tisaicore.exception.InsufficientStockException;
import br.com.tisaicore.repository.StockMovementRepository;
import org.springframework.stereotype.Component;

/**
 * Garante que toda alteração de estoque (produto/lote) seja registrada como
 * StockMovement auditável. Depende apenas do repositório de movimentos — sem
 * ciclos com ProductService/StockService/OrderService.
 */
@Component
public class StockMovementWriter {

    private final StockMovementRepository repository;

    public StockMovementWriter(StockMovementRepository repository) {
        this.repository = repository;
    }

    public StockMovement record(Product product, MovementType type, int quantity,
                                String reason, User user, Batch batch) {
        switch (type) {
            case IN -> {
                product.setStockQuantity(product.getStockQuantity() + quantity);
                if (batch != null) {
                    batch.setCurrentQuantity(batch.getCurrentQuantity() + quantity);
                }
            }
            case OUT -> {
                if (product.getStockQuantity() < quantity) {
                    throw new InsufficientStockException(
                            product.getName(), product.getStockQuantity(), quantity);
                }
                product.setStockQuantity(product.getStockQuantity() - quantity);
                if (batch != null) {
                    if (batch.getCurrentQuantity() < quantity) {
                        throw new InsufficientStockException(
                                "Lote " + batch.getCode(), batch.getCurrentQuantity(), quantity);
                    }
                    batch.setCurrentQuantity(batch.getCurrentQuantity() - quantity);
                }
            }
            case ADJUSTMENT -> {
                product.setStockQuantity(quantity);
                if (batch != null) {
                    batch.setCurrentQuantity(quantity);
                }
            }
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setUser(user);
        movement.setBatch(batch);
        return repository.save(movement);
    }
}
