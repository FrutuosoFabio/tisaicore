package br.com.tisaicore.controller;

import br.com.tisaicore.dto.response.VitrineAdminResponse;
import br.com.tisaicore.service.VitrineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vitrine")
public class VitrineController {

    private final VitrineService vitrineService;

    public VitrineController(VitrineService vitrineService) {
        this.vitrineService = vitrineService;
    }

    @GetMapping
    public ResponseEntity<List<VitrineAdminResponse>> list() {
        return ResponseEntity.ok(vitrineService.listAdmin());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<VitrineAdminResponse> add(@PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vitrineService.addProduct(productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        vitrineService.removeProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
