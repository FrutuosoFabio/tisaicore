package br.com.tisaicore.controller;

import br.com.tisaicore.dto.request.CreateBatchRequest;
import br.com.tisaicore.dto.request.UpdateBatchRequest;
import br.com.tisaicore.dto.response.BatchResponse;
import br.com.tisaicore.service.BatchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    public ResponseEntity<BatchResponse> create(@Valid @RequestBody CreateBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<BatchResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(batchService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(batchService.findById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<BatchResponse>> findByProduct(
            @PathVariable("productId") Long productId, Pageable pageable) {
        return ResponseEntity.ok(batchService.findByProductId(productId, pageable));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<BatchResponse>> findExpired() {
        return ResponseEntity.ok(batchService.findExpired());
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<BatchResponse>> findExpiringSoon(
            @RequestParam(name = "days", defaultValue = "30") int days) {
        return ResponseEntity.ok(batchService.findExpiringSoon(days));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BatchResponse> update(
            @PathVariable("id") Long id, @Valid @RequestBody UpdateBatchRequest request) {
        return ResponseEntity.ok(batchService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        batchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
