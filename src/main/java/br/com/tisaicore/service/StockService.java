package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.StockMovementRequest;
import br.com.tisaicore.dto.response.StockMovementResponse;
import br.com.tisaicore.entity.Batch;
import br.com.tisaicore.entity.MovementType;
import br.com.tisaicore.entity.Product;
import br.com.tisaicore.entity.StockMovement;
import br.com.tisaicore.entity.User;
import br.com.tisaicore.exception.InsufficientStockException;
import br.com.tisaicore.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductService productService;
    private final UserService userService;
    private final BatchService batchService;

    public StockService(StockMovementRepository stockMovementRepository,
                        ProductService productService,
                        UserService userService,
                        BatchService batchService) {
        this.stockMovementRepository = stockMovementRepository;
        this.productService = productService;
        this.userService = userService;
        this.batchService = batchService;
    }

    @Transactional
    public StockMovementResponse createMovement(StockMovementRequest request, Long userId) {
        Product product = productService.findEntityById(request.productId());
        User user = userService.findEntityById(userId);

        Batch batch = null;
        if (request.batchId() != null) {
            batch = batchService.findEntityById(request.batchId());
            if (!batch.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("Lote não pertence ao produto especificado");
            }
        }

        if (request.type() == MovementType.OUT) {
            if (product.getStockQuantity() < request.quantity()) {
                throw new InsufficientStockException(
                        product.getName(), product.getStockQuantity(), request.quantity());
            }
            product.setStockQuantity(product.getStockQuantity() - request.quantity());
            if (batch != null) {
                if (batch.getCurrentQuantity() < request.quantity()) {
                    throw new InsufficientStockException(
                            "Batch " + batch.getCode(), batch.getCurrentQuantity(), request.quantity());
                }
                batch.setCurrentQuantity(batch.getCurrentQuantity() - request.quantity());
            }
        } else if (request.type() == MovementType.IN) {
            product.setStockQuantity(product.getStockQuantity() + request.quantity());
            if (batch != null) {
                batch.setCurrentQuantity(batch.getCurrentQuantity() + request.quantity());
            }
        } else {
            product.setStockQuantity(request.quantity());
            if (batch != null) {
                batch.setCurrentQuantity(request.quantity());
            }
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(request.type());
        movement.setQuantity(request.quantity());
        movement.setReason(request.reason());
        movement.setUser(user);
        movement.setBatch(batch);

        return StockMovementResponse.from(stockMovementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> findAll(Pageable pageable) {
        return stockMovementRepository.findAll(pageable).map(StockMovementResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> findByProductId(Long productId, Pageable pageable) {
        return stockMovementRepository.findByProductId(productId, pageable)
                .map(StockMovementResponse::from);
    }
}
