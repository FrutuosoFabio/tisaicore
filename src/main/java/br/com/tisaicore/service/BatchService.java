package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.CreateBatchRequest;
import br.com.tisaicore.dto.request.UpdateBatchRequest;
import br.com.tisaicore.dto.response.BatchResponse;
import br.com.tisaicore.entity.Batch;
import br.com.tisaicore.entity.Product;
import br.com.tisaicore.exception.ResourceNotFoundException;
import br.com.tisaicore.repository.BatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final ProductService productService;

    public BatchService(BatchRepository batchRepository, ProductService productService) {
        this.batchRepository = batchRepository;
        this.productService = productService;
    }

    @Transactional
    public BatchResponse create(CreateBatchRequest request) {
        if (batchRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Batch code already exists: " + request.code());
        }

        Product product = productService.findEntityById(request.productId());

        if (request.manufacturingDate() != null && request.expirationDate() != null
                && request.manufacturingDate().isAfter(request.expirationDate())) {
            throw new IllegalArgumentException("Manufacturing date cannot be after expiration date");
        }

        Batch batch = new Batch();
        batch.setCode(request.code());
        batch.setProduct(product);
        batch.setExpirationDate(request.expirationDate());
        batch.setManufacturingDate(request.manufacturingDate());
        batch.setSupplier(request.supplier());
        batch.setInitialQuantity(request.initialQuantity());
        batch.setCurrentQuantity(request.initialQuantity());
        batch.setNotes(request.notes());

        return BatchResponse.from(batchRepository.save(batch));
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findAll(Pageable pageable) {
        return batchRepository.findAll(pageable).map(BatchResponse::from);
    }

    @Transactional(readOnly = true)
    public BatchResponse findById(Long id) {
        return BatchResponse.from(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findByProductId(Long productId, Pageable pageable) {
        return batchRepository.findByProductId(productId, pageable).map(BatchResponse::from);
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> findExpired() {
        return batchRepository.findByExpirationDateBeforeAndActiveTrue(LocalDate.now())
                .stream().map(BatchResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> findExpiringSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(days);
        return batchRepository.findByExpirationDateBetweenAndActiveTrue(today, limit)
                .stream().map(BatchResponse::from).toList();
    }

    @Transactional
    public BatchResponse update(Long id, UpdateBatchRequest request) {
        Batch batch = findEntityById(id);

        if (request.expirationDate() != null) {
            batch.setExpirationDate(request.expirationDate());
        }
        if (request.manufacturingDate() != null) {
            batch.setManufacturingDate(request.manufacturingDate());
        }
        if (request.supplier() != null) {
            batch.setSupplier(request.supplier());
        }
        if (request.notes() != null) {
            batch.setNotes(request.notes());
        }

        if (batch.getManufacturingDate() != null && batch.getExpirationDate() != null
                && batch.getManufacturingDate().isAfter(batch.getExpirationDate())) {
            throw new IllegalArgumentException("Manufacturing date cannot be after expiration date");
        }

        return BatchResponse.from(batchRepository.save(batch));
    }

    @Transactional
    public void delete(Long id) {
        Batch batch = findEntityById(id);
        batch.setActive(false);
        batchRepository.save(batch);
    }

    public Batch findEntityById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", id));
    }
}
