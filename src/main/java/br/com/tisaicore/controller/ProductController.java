package br.com.tisaicore.controller;

import br.com.tisaicore.dto.request.CreateProductRequest;
import br.com.tisaicore.dto.response.ProductResponse;
import br.com.tisaicore.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            Pageable pageable,
            @RequestParam(name = "image", defaultValue = "false") boolean image) {
        return ResponseEntity.ok(productService.findAll(pageable, image));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable("id") Long id,
            @RequestParam(name = "image", defaultValue = "false") boolean image) {
        return ResponseEntity.ok(productService.findById(id, image));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("stockQuantity");
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantidade de estoque inválida");
        }
        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
