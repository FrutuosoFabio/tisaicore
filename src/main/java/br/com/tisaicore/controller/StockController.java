package br.com.tisaicore.controller;

import br.com.tisaicore.dto.request.StockMovementRequest;
import br.com.tisaicore.dto.response.StockMovementResponse;
import br.com.tisaicore.service.StockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/movements")
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody StockMovementRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockService.createMovement(request, userId));
    }

    @GetMapping("/movements")
    public ResponseEntity<Page<StockMovementResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(stockService.findAll(pageable));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Page<StockMovementResponse>> findByProduct(
            @PathVariable Long productId, Pageable pageable) {
        return ResponseEntity.ok(stockService.findByProductId(productId, pageable));
    }
}
