package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.StockMovementRequest;
import br.com.tisaicore.dto.response.StockMovementResponse;
import br.com.tisaicore.entity.Batch;
import br.com.tisaicore.entity.Product;
import br.com.tisaicore.entity.StockMovement;
import br.com.tisaicore.entity.User;
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
    private final StockMovementWriter stockMovementWriter;

    public StockService(StockMovementRepository stockMovementRepository,
                        ProductService productService,
                        UserService userService,
                        BatchService batchService,
                        StockMovementWriter stockMovementWriter) {
        this.stockMovementRepository = stockMovementRepository;
        this.productService = productService;
        this.userService = userService;
        this.batchService = batchService;
        this.stockMovementWriter = stockMovementWriter;
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

        StockMovement movement = stockMovementWriter.record(
                product, request.type(), request.quantity(), request.reason(), user, batch);
        return StockMovementResponse.from(movement);
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
