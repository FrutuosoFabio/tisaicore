package br.com.tisaicore.controller;

import br.com.tisaicore.dto.response.VitrinePublicResponse;
import br.com.tisaicore.service.VitrineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicVitrineController {

    private final VitrineService vitrineService;

    public PublicVitrineController(VitrineService vitrineService) {
        this.vitrineService = vitrineService;
    }

    @GetMapping("/vitrine")
    public ResponseEntity<List<VitrinePublicResponse>> listVitrine() {
        return ResponseEntity.ok(vitrineService.listPublic());
    }
}
